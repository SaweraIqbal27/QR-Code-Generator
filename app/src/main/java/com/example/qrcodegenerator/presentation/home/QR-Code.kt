package com.example.qrcodegenerator.presentation.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.qrcodegenerator.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun gerarQRCode(qrCodeData: String): Bitmap? {
    val qrCodeWriter = QRCodeWriter()
    return try {
        val bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 196, 196)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }
}

@Composable
fun QR() {
    var text by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().paint(painterResource(id = R.drawable.mobilewallpaper), contentScale = ContentScale.FillBounds), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "QR Code Generator", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = text, onValueChange = {
                text = it
            },
            label = { Text(text = "Type Something",color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Gray,
                unfocusedIndicatorColor = Color.LightGray,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.LightGray,
                focusedTextColor = Color.Gray,
                cursorColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (text.isNotEmpty()) {
                    bitmap = gerarQRCode(text)
                } else {
                    Toast.makeText(context, "Type Something", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.width(280.dp), colors = ButtonDefaults.buttonColors(Color.Gray)) {
            Text(text = "Generate QR Code", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            val uri = saveBitmapToCache(context, bitmap!!)
            uri?.let {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newUri(context.contentResolver, "QR Code", it)
                clipboard.setPrimaryClip(clip)
                shareImage(context, it)
            }
        }, colors = ButtonDefaults.buttonColors(Color.Gray)) {
            Text("Share QR Code", color = Color.White, fontWeight = FontWeight.Bold)
        }


        Spacer(modifier = Modifier.height(16.dp))
        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null)
        }

    }
}


fun saveBitmapToCache(context: android.content.Context, bitmap: Bitmap): Uri? {
    val cachePath = File(context.cacheDir, "images")
    return try {
        cachePath.mkdirs()
        val file = File(cachePath, "qr_code.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun shareImage(context: android.content.Context, uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant permission to read the URI
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
}