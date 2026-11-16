package rcstorybook.stories

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.compose.layout.RemoteCanvas
import ee.schimke.composeai.rccreation.compose.layout.RemoteComposable
import ee.schimke.composeai.rccreation.compose.layout.RemoteOffset
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.size
import ee.schimke.composeai.rccreation.compose.state.RemoteFloat
import ee.schimke.composeai.rccreation.compose.state.RemotePaint
import ee.schimke.composeai.rccreation.compose.state.RemotePaintingStyle
import ee.schimke.composeai.rccreation.compose.state.RemoteTimeVariables
import ee.schimke.composeai.rccreation.compose.state.abs
import ee.schimke.composeai.rccreation.compose.state.acos
import ee.schimke.composeai.rccreation.compose.state.asin
import ee.schimke.composeai.rccreation.compose.state.atan
import ee.schimke.composeai.rccreation.compose.state.atan2
import ee.schimke.composeai.rccreation.compose.state.cbrt
import ee.schimke.composeai.rccreation.compose.state.ceil
import ee.schimke.composeai.rccreation.compose.state.clamp
import ee.schimke.composeai.rccreation.compose.state.copySign
import ee.schimke.composeai.rccreation.compose.state.cos
import ee.schimke.composeai.rccreation.compose.state.exp
import ee.schimke.composeai.rccreation.compose.state.floor
import ee.schimke.composeai.rccreation.compose.state.lerp
import ee.schimke.composeai.rccreation.compose.state.ln
import ee.schimke.composeai.rccreation.compose.state.log
import ee.schimke.composeai.rccreation.compose.state.mad
import ee.schimke.composeai.rccreation.compose.state.max
import ee.schimke.composeai.rccreation.compose.state.min
import ee.schimke.composeai.rccreation.compose.state.pow
import ee.schimke.composeai.rccreation.compose.state.rc
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.round
import ee.schimke.composeai.rccreation.compose.state.sign
import ee.schimke.composeai.rccreation.compose.state.sin
import ee.schimke.composeai.rccreation.compose.state.sqrt
import ee.schimke.composeai.rccreation.compose.state.tan
import ee.schimke.composeai.rccreation.compose.state.toDeg
import ee.schimke.composeai.rccreation.compose.state.toRad

/**
 * Applies the named RemoteFloat function to x. Every case is an expression the player evaluates.
 */
fun remoteFunction(name: String, x: RemoteFloat): RemoteFloat =
    when (name) {
        "abs" -> abs(x)
        "acos" -> acos(clamp(x, (-1f).rf, 1f.rf))
        "asin" -> asin(clamp(x, (-1f).rf, 1f.rf))
        "atan" -> atan(x)
        "atan2" -> atan2(x, 1f.rf)
        "cbrt" -> cbrt(x)
        "ceil" -> ceil(x)
        "clamp" -> clamp(x, (-0.5f).rf, 0.5f.rf)
        "copySign" -> copySign(0.5f.rf, x)
        "cos" -> cos(x * 3f.rf)
        "exp" -> exp(x) / 3f.rf
        "floor" -> floor(x)
        "lerp" -> lerp((-1f).rf, 1f.rf, (x + 1f.rf) / 2f.rf)
        "ln" -> ln(abs(x) + 0.05f.rf) / 3f.rf
        "log" -> log(abs(x) + 0.05f.rf)
        "mad" -> mad(x, 0.5f.rf, 0.25f.rf)
        "max" -> max(x, 0f.rf)
        "min" -> min(x, 0f.rf)
        "pow" -> pow(x, 2f.rf)
        "round" -> round(x * 2f.rf) / 2f.rf
        "sign" -> sign(x)
        "sqrt" -> sqrt(abs(x))
        "tan" -> tan(x) / 3f.rf
        "toDeg" -> toDeg(x) / 180f.rf
        "toRad" -> toRad(x * 90f.rf)
        else -> sin(x * 3f.rf)
    }

@Composable
@RemoteComposable
fun FloatFunctionPlot(function: String, animate: Boolean, color: Color) {
    RemoteCanvas(modifier = RemoteModifier.size(320.rdp, 220.rdp)) {
        val axis = RemotePaint {
            this.color = Color(0x44000000).rc
            style = RemotePaintingStyle.Stroke
            strokeWidth = 1f.rf
        }
        drawLine(axis, RemoteOffset(0f.rf, height / 2f.rf), RemoteOffset(width, height / 2f.rf))
        drawLine(axis, RemoteOffset(width / 2f.rf, 0f.rf), RemoteOffset(width / 2f.rf, height))
        val dot = RemotePaint { this.color = color.rc }
        val shift = if (animate) sin(RemoteTimeVariables.continuousSeconds) * 0.5f.rf else 0f.rf
        // 61 samples of y = f(x) for x in [-1, 1]; the player computes each y.
        for (i in 0..60) {
            val x = -1f + i / 30f
            val y = remoteFunction(function, x.rf + shift)
            drawCircle(
                dot,
                radius = 3f.rf,
                center =
                    RemoteOffset(
                        width * ((x + 1f) / 2f).rf,
                        height / 2f.rf - y * (height / 2.4f.rf),
                    ),
            )
        }
    }
}
