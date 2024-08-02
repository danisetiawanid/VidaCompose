package id.vida.sampleapp.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.dani.vidacompose.constant.KeyConstant
import id.vida.liveness.VIDAException
import id.vida.liveness.VidaLiveness
import id.vida.liveness.config.VidaFaceDetectionOption
import id.vida.liveness.config.VidaUICustomizationOption
import id.vida.liveness.constants.CameraType
import id.vida.liveness.constants.Gestures
import id.vida.liveness.constants.Shape
import id.vida.liveness.dto.VidaLivenessRequest
import id.vida.liveness.dto.VidaLivenessResponse
import id.vida.liveness.listeners.VidaLivenessListener
import java.lang.ref.WeakReference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                LivenessScreen()
            }
        }
    }

    @Composable
    fun LivenessScreen() {
        val context = LocalContext.current
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var livenessResult by remember { mutableStateOf("") }
        var transactionId by remember { mutableStateOf("") }
        var activeLiveness by remember { mutableStateOf(false) }
        var livenessDetection by remember { mutableStateOf<VidaLiveness?>(null) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            imageBitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "Liveness Result")
            }

            Text(text = livenessResult)
            Text(text = transactionId)

            Button(onClick = {
                livenessDetection = startLiveness(
                    context,
                    CameraType.FRONT,
                    KeyConstant.API_KEY_FRONT,
                    KeyConstant.LICENSE_KEY_FRONT,
                    activeLiveness,
                    { response ->
                        imageBitmap =
                            response.imageBytes?.let {
                                BitmapFactory.decodeByteArray(
                                    response.imageBytes,
                                    0,
                                    it.size
                                )
                            }
                        livenessResult =
                            "Success - Liveness Score: ${response.livenessScore} Image Manipulation Score: ${response.manipulationScore}"
                        transactionId =
                            "SDK Version: ${livenessDetection?.sdkVersion} Transaction ID: ${response.transactionId}"
                    },
                    { errorCode, errorMessage, response ->
                        imageBitmap =
                            if (response.imageBytes != null) {
                                BitmapFactory.decodeByteArray(
                                    response.imageBytes,
                                    0,
                                    response.imageBytes!!.size
                                )
                            } else {
                                null
                            }
                        livenessResult =
                            "Code: $errorCode $errorMessage : LIVENESS SCORE: ${response.livenessScore} : Image Manipulation Score ${response.manipulationScore}"
                        transactionId = "Transaction ID: ${response.transactionId}"
                    })
            }) {
                Text("Start Liveness")
            }

            Switch(
                checked = activeLiveness,
                onCheckedChange = { activeLiveness = it }
            )

            Text(text = "Active Liveness: $activeLiveness")
        }
    }

    private fun startLiveness(
        context: Context,
        cameraType: CameraType,
        apiKey: String,
        licenseKey: String,
        activeLiveness: Boolean,
        onSuccess: (VidaLivenessResponse) -> Unit,
        onError: (Int, String, VidaLivenessResponse) -> Unit
    ): VidaLiveness? {
        var livenessDetection: VidaLiveness? = null
        val livenessRequest = VidaLivenessRequest().apply {
            this.apiKey = apiKey
            this.licenseKey = licenseKey
        }

        val allowedGestures = HashSet<Gestures>().apply {
            add(Gestures.BLINK)
            add(Gestures.SMILE)
            add(Gestures.SHAKE_HEAD)
        }

        try {
            val activity = WeakReference(context as Activity)
            livenessDetection = VidaLiveness.VidaLivenessBuilder.newInstance(
                activity,
                livenessRequest,
                object : VidaLivenessListener {
                    override fun onSuccess(response: VidaLivenessResponse) {
                        onSuccess(response)
                    }

                    override fun onError(
                        errorCode: Int,
                        errorMessage: String,
                        response: VidaLivenessResponse
                    ) {
                        onError(errorCode, errorMessage, response)
                    }

                    override fun onInitialized() {
                        livenessDetection?.startDetection()
                    }
                })
                .setDetectionOptions(
                    VidaFaceDetectionOption.VidaFaceDetectionOptionBuilder.newInstance()
                        .setAllowedGestures(allowedGestures)
                        .setEnableActiveLiveness(activeLiveness)
                        .build()
                )
                .setUICustomizationOptions(
                    VidaUICustomizationOption.VidaUICustomizationOptionBuilder.newInstance()
                        .setCameraType(cameraType)
                        .setShowTutorialScreen(true)
                        .setShowReviewScreen(false)
                        .setOverlayShape(Shape.CIRCLE)
                        .build()
                )
                .build()
            livenessDetection?.initialize()
        } catch (e: VIDAException) {
            Toast.makeText(context, "Liveness failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return livenessDetection
    }
}