"""Tests for the coverage rule in storybook_tools.py: python3 -m unittest discover -s tools"""

import json
import os
import tempfile
import types
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
        # A pending block offered by a control option used to count as covered on the site and as
        # pending in the CLI: one rule now decides both.
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


class ReviewFindingsTest(unittest.TestCase):
    def test_member_of_another_object_is_not_a_modifier(self):
        # `RemoteFontVariation.width(` is not the `width` modifier, and inside a row
        # `RemoteFontVariation.weight(` is not RemoteRowScope.weight.
        width = member("WidthModifierKt", "width")
        code = "RemoteText(t, settings = RemoteFontVariation.Settings(RemoteFontVariation.width(w)))"
        self.assertFalse(used(width, code, (C + "modifier.width",)))
        self.assertTrue(used(width, "RemoteModifier.width(9.rdp)", (C + "modifier.width",)))
        self.assertTrue(used(width, "RemoteModifier.size(1.rdp)\n    .width(9.rdp)", (C + "modifier.width",)))
        weight = member("RemoteRowScope", "weight")
        in_row = "RemoteRow { RemoteText(t, fontVariationSettings = RemoteFontVariation.weight(1f.rf)) }"
        self.assertFalse(used(weight, in_row))

    def test_a_parameter_named_like_an_import_does_not_keep_it(self):
        index = t.ApiIndex(ENTRIES)
        body = "fun Story(width: Float) {\n    RemoteText(t, RemoteFontVariation.width(width.rf))\n}"
        self.assertFalse(t.import_needed(C + "modifier.width", body, [C + "modifier.width"], ENTRIES, index))
        self.assertTrue(
            t.import_needed(
                C + "modifier.width", "RemoteModifier.width(1.rdp)", [C + "modifier.width"], ENTRIES, index
            )
        )
        # Not AndroidX remote API: the plain word rule.
        self.assertTrue(
            t.import_needed("androidx.compose.ui.graphics.Color", "Color.Red", [], ENTRIES, index)
        )

    def test_explicit_receivers_of_a_lambda_receiver(self):
        draw = member("RemoteDrawScope", "drawCircle")
        self.assertTrue(used(draw, "RemoteCanvas { this.drawCircle(p, 1f.rf) }"))
        self.assertTrue(used(draw, "fun dot(scope: RemoteDrawScope) {\n    scope.drawCircle(p, 1f.rf)\n}"))
        self.assertTrue(
            used(draw, "fun dot(scope: RemoteDrawScope) {\n    with(scope) { drawCircle(p, 1f.rf) }\n}")
        )
        self.assertTrue(used(draw, "val d: RemoteDrawScope.() -> Unit = { drawCircle(p, 1f.rf) }"))
        self.assertFalse(used(draw, "fun dot(scope: Canvas) {\n    scope.drawCircle(p, 1f.rf)\n}"))

    def test_expression_bodied_extension(self):
        draw = member("RemoteDrawScope", "drawCircle")
        self.assertTrue(used(draw, "fun RemoteDrawScope.dot() = drawCircle(p, 1f.rf)\n"))
        weight = member("RemoteRowScope", "weight")
        code = "fun RemoteRowScope.x() = 1\n\nfun y() {\n    RemoteText(t, RemoteModifier.weight(1f.rf))\n}"
        self.assertFalse(used(weight, code))

    def test_object_member_imported_directly(self):
        year = member("RemoteTimeVariables", "year")
        self.assertTrue(
            used(year, "RemoteText(year.toRemoteString())", (C + "state.RemoteTimeVariables.year",))
        )
        self.assertFalse(used(year, "RemoteText(year.toRemoteString())", ()))

    def test_function_reference(self):
        tween = member("RemoteAnimationKt", "remoteTween")
        self.assertTrue(used(tween, "val make = ::remoteTween", (C + "state.remoteTween",)))

    def test_nested_generic_call(self):
        enum = member("RemoteStateKt", "rememberNamedRemoteEnum")
        code = 'rememberNamedRemoteEnum<Pair<Screen, List<Int>>>("s", x)'
        self.assertTrue(used(enum, code, (C + "state.rememberNamedRemoteEnum",)))

    def test_option_codes_unescape_kotlin_strings(self):
        with tempfile.TemporaryDirectory() as d:
            with open(os.path.join(d, "C.kt"), "w", encoding="utf-8") as fh:
                fh.write('option("A", "RemoteState.Domain(\\"weather\\")", x)\n')
                fh.write('option("B", "a\\\\b \\$c \\u0041\\tz", y)\n')
            self.assertEqual(t.catalog_option_codes(d), ['RemoteState.Domain("weather")', "a\\b $c A\tz"])


class CoverageExitCodeTest(unittest.TestCase):
    """cmd_coverage fails on a missing block and on a stale CANNOT_DEMONSTRATE reason."""

    NAME = "RemoteEasing.Linear"

    def run_coverage(self, options, reasons, report_only=False):
        block = {k: v for k, v in t.building_blocks(ENTRIES).items() if k[1] == self.NAME}
        with tempfile.TemporaryDirectory() as d:
            sources = os.path.join(d, "sources.json")
            with open(sources, "w", encoding="utf-8") as fh:
                json.dump({}, fh)
            catalog = os.path.join(d, "catalog")
            os.makedirs(catalog)
            with open(os.path.join(catalog, "C.kt"), "w", encoding="utf-8") as fh:
                fh.write("".join(f'option("x", "{o}", 1)\n' for o in options))
            args = types.SimpleNamespace(
                api=API,
                sources=sources,
                catalog=catalog,
                out=os.path.join(d, "c.json"),
                report_only=report_only,
            )
            with (
                mock.patch.object(t, "building_blocks", return_value=block),
                mock.patch.dict(t.CANNOT_DEMONSTRATE, reasons, clear=True),
                mock.patch("sys.stdout"),
                mock.patch("sys.stderr"),
            ):
                return t.cmd_coverage(args)

    def test_covered(self):
        self.assertEqual(self.run_coverage([self.NAME], {}), 0)

    def test_missing_fails(self):
        self.assertEqual(self.run_coverage([], {}), 1)

    def test_pending_passes(self):
        self.assertEqual(self.run_coverage([], {self.NAME: "why"}), 0)

    def test_stale_reason_fails(self):
        self.assertEqual(self.run_coverage([self.NAME], {self.NAME: "why"}), 1)

    def test_report_only_passes(self):
        self.assertEqual(self.run_coverage([], {}, report_only=True), 0)
        self.assertEqual(self.run_coverage([self.NAME], {self.NAME: "why"}, report_only=True), 0)


if __name__ == "__main__":
    unittest.main()


class WearMaterial3Test(unittest.TestCase):
    W = "androidx.wear.compose.remote.material3."

    def test_package_is_rewritten_to_androidx(self):
        self.assertEqual(
            t.rewrite("import ee.schimke.composeai.rccreation.wear.material3.RemoteButton"),
            "import " + self.W + "RemoteButton",
        )

    def test_every_wear_composable_is_a_building_block(self):
        blocks = t.building_blocks(ENTRIES)
        wear = {name for group, name in blocks if group == "Wear Material 3"}
        self.assertIn("RemoteButton", wear)
        self.assertIn("RemoteEdgeButton", wear)
        self.assertIn("RemoteTimeText", wear)
        # Defaults objects and state factories are reached through the composables, not blocks.
        self.assertNotIn("RemoteButtonDefaults", wear)
        self.assertNotIn("rememberRemotePageIndicatorState", wear)

    def test_wear_text_is_not_the_layout_text(self):
        # Same simple name in two packages: the Wear one counts only with the Wear import.
        found = t.find_members(ENTRIES, "RemoteTextKt.RemoteText")
        wear = t.block_specs([e for e in found if e["package"] + "." == self.W])
        self.assertTrue(wear)
        code = 'fun F() { RemoteText("a".rs) }'
        self.assertTrue(used(wear, code, [self.W + "RemoteText"]))
        self.assertFalse(used(wear, code, [C + "layout.RemoteText"]))

    def test_import_outside_the_public_wear_api_fails(self):
        with tempfile.TemporaryDirectory() as d:
            stories = os.path.join(d, "stories")
            os.makedirs(stories)
            with open(os.path.join(stories, "S.kt"), "w", encoding="utf-8") as fh:
                fh.write(
                    "package rcstorybook.stories\n\n"
                    "import ee.schimke.composeai.rccreation.wear.material3.RemoteSlider\n"
                )
            args = types.SimpleNamespace(
                stories=stories,
                api=API,
                out=os.path.join(d, "out", "sources.json"),
                verify_out=None,
                snippets_out=None,
            )
            with mock.patch("sys.stderr"):
                self.assertEqual(t.cmd_sources(args), 1)
