package com.eventmanagement.backend.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

@Service
class QrCodeService {

    fun generateQrCodeBase64(data: String, width: Int = 300, height: Int = 300): String {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height)

        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)

        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }
}