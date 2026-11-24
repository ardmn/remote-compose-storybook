"""Tests for the coverage rule in storybook_tools.py: python3 -m unittest discover -s tools"""

import os
import unittest
from unittest import mock

import storybook_tools as t

API = os.path.join(os.path.dirname(__file__), "..", "reference", "androidx-api")
ENTRIES = t.parse_api(API)
INDEX = t.ApiIndex(ENTRIES)
C = "androidx.compose.remote.creation.compose."


def member(owner, name):
    """The use specs of `Owner.name` (a member, or an extension on Owner or Owner.Companion)."""
    found = t.find_members(ENTRIES, owner + "." + name)
    if not found:
        raise AssertionError(f"{owner}.{name} is not in the API files")
    return t.block_specs(found)


def used(specs, code, imports=()):
    return any(t.member_used(s, t.strip_code(code), set(imports), INDEX) for s in specs)


class SemanticsTest(unittest.TestCase):
    IMPORTS = (C + "modifier.semantics", C + "modifier.text", C + "modifier.contentDescription")

    def test_property_set_inside_semantics_block(self):
        code = "RemoteModifier.semantics { text = label.rs }"
        self.assertTrue(used(member("RemoteSemanticsPropertyReceiver", "text"), code, self.IMPORTS))

    def test_property_set_inside_clear_and_set_semantics(self):
        code = "RemoteModifier.size(1.rdp)\n    .clearAndSetSemantics {\n        text = a\n    }"
        self.assertTrue(used(member("RemoteSemanticsPropertyReceiver", "text"), code, self.IMPORTS))

    def test_same_name_outside_semantics_does_not_count(self):
        code = (
            "RemoteText(text = label.rs)\nval text = 1\nRemoteModifier.semantics { contentDescription = d }"
        )
        self.assertFalse(used(member("RemoteSemanticsPropertyReceiver", "text"), code, self.IMPORTS))

    def test_needs_its_import(self):
        code = "RemoteModifier.semantics { text = label.rs }"
        self.assertFalse(
            used(member("RemoteSemanticsPropertyReceiver", "text"), code, (C + "modifier.semantics",))
        )


class ReceiverTest(unittest.TestCase):
    def test_brush_image_on_remote_brush_only(self):
        specs = member("RemoteBrush", "image")
        imports = (C + "shaders.image",)
        self.assertTrue(used(specs, "background(RemoteBrush.image(bitmap))", imports))
        self.assertFalse(used(specs, "RemoteImage(image(bitmap), null)", imports))

    def test_modifier_rotate_is_not_draw_scope_rotate(self):
        modifier = member("RotateModifierKt", "rotate")
        draw = member("RemoteDrawScope", "rotate")
        imports = (C + "modifier.rotate",)
        on_modifier = "RemoteBox(RemoteModifier.rotate(45f.rf)) {}"
        in_canvas = "RemoteCanvas(RemoteModifier.size(9.rdp)) {\n    rotate(45f.rf) { drawRect(paint) }\n}"
        self.assertTrue(used(modifier, on_modifier, imports))
        self.assertFalse(used(draw, on_modifier, imports))
        self.assertTrue(used(draw, in_canvas))
        self.assertFalse(used(modifier, in_canvas, imports))

    def test_draw_scope_member_needs_a_draw_scope(self):
        draw = member("RemoteDrawScope", "drawRect")
        self.assertFalse(used(draw, "Canvas(bitmap).apply { drawRect(0f, 0f, 1f, 1f, Paint()) }"))
        self.assertTrue(used(draw, "RemoteModifier.drawWithContent { drawRect(paint) }"))

    def test_row_scope_weight_is_not_font_variation_weight(self):
        weight = member("RemoteRowScope", "weight")
        self.assertTrue(used(weight, "RemoteRow { RemoteText(t, modifier = RemoteModifier.weight(1f.rf)) }"))
        self.assertFalse(used(weight, "RemoteFontVariation.Settings(RemoteFontVariation.weight(4f.rf))"))

    def test_innermost_receiver_wins(self):
        # In withTransform's first lambda `rotate` is RemoteDrawTransform's, in its trailing one
        # RemoteDrawScope's; in a column inside a row `weight` is the column's.
        draw = member("RemoteDrawScope", "rotate")
        transform = member("RemoteDrawTransform", "rotate")
        in_transform = "RemoteCanvas {\n    withTransform({ rotate(9f.rf) }) { drawRect(paint) }\n}"
        in_draw = (
            "RemoteCanvas {\n    withTransform({ translate(1f.rf) }) { rotate(9f.rf) { drawRect(paint) } }\n}"
        )
        self.assertFalse(used(draw, in_transform))
        self.assertTrue(used(transform, in_transform))
        self.assertTrue(used(draw, in_draw))
        self.assertFalse(used(transform, in_draw))
        nested = "RemoteRow {\n    RemoteColumn(m) { RemoteText(t, RemoteModifier.weight(1f.rf)) }\n}"
        self.assertFalse(used(member("RemoteRowScope", "weight"), nested))
        self.assertTrue(used(member("RemoteColumnScope", "weight"), nested))

    def test_named_lambda_argument(self):
        draw = member("RemoteDrawScope", "drawRect")
        self.assertTrue(used(draw, "RemoteCanvas(modifier = m, onDraw = { drawRect(paint) })"))

    def test_object_member_through_an_alias(self):
        year = member("RemoteTimeVariables", "year")
        self.assertTrue(used(year, "val time = RemoteTimeVariables\nRemoteText(time.year.toRemoteString())"))
        self.assertFalse(used(year, "val time = RemoteTimeVariables.hourOfDay\nval y = date.year"))

    def test_factory_function_needs_a_call(self):
        factory = member("RemoteColorKt", "RemoteColor")
        imports = (C + "state.RemoteColor",)
        self.assertFalse(used(factory, "val c: RemoteColor = RemoteColor.White", imports))
        self.assertTrue(used(factory, "val c = RemoteColor(0xFF000000.toInt())", imports))


class StripCodeTest(unittest.TestCase):
    def test_strings_and_comments_are_not_uses(self):
        draw = member("RemoteDrawScope", "drawRect")
        self.assertFalse(used(draw, 'RemoteCanvas { // drawRect(paint)\n    val s = "drawRect(" }'))

    def test_template_expressions_are_kept(self):
        prefixed = member("RemoteState.Domain", "prefixed")
        self.assertTrue(used(prefixed, 'RemoteText("host sets: ${domain.prefixed("city")}".rs)'))

    def test_length_is_kept(self):
        code = 'val c = \'\\\'\' + """a ${b.c()} """ // x\n/* y */ z'
        self.assertEqual(len(t.strip_code(code)), len(code))


class StatusTest(unittest.TestCase):
    """One rule decides each block's status; covered wins over a CANNOT_DEMONSTRATE reason."""

    def report(self, code, imports, options=()):
        sources = {"Story": {"code": code, "imports": list(imports)}}
        return t.coverage_report(ENTRIES, sources, list(options))

    def test_pending_block_covered_by_an_option_is_covered(self):
        # A pending block offered by a control option counted as covered on the site and as
        # pending in the CLI (ardmn/work#11).
        with mock.patch.dict(t.CANNOT_DEMONSTRATE, {"RemoteEasing.Linear": "a reason"}):
            report = self.report("", (), ["RemoteEasing.Linear"])
        blocks = {b["name"]: b for b in report["blocks"]}
        self.assertEqual(blocks["RemoteEasing.Linear"]["status"], "covered")
        self.assertEqual(blocks["RemoteEasing.Linear"]["optionCodes"], ["RemoteEasing.Linear"])
        self.assertEqual(blocks["drawTextOnCircle"]["status"], "pending")
        statuses = [b["status"] for b in report["blocks"]]
        summary = report["summary"]
        self.assertEqual(summary["covered"], statuses.count("covered"))
        self.assertEqual(summary["pending"], statuses.count("pending"))
        self.assertEqual(summary["missing"], statuses.count("missing"))
        self.assertEqual(summary["total"], len(statuses))

    def test_covered_beats_a_reason(self):
        code = 'val l = rememberNamedRemoteLong("n", 1L)'
        report = self.report(code, [C + "state.rememberNamedRemoteLong"])
        blocks = {b["name"]: b for b in report["blocks"]}
        self.assertEqual(blocks["rememberNamedRemoteLong"]["status"], "covered")


if __name__ == "__main__":
    unittest.main()
