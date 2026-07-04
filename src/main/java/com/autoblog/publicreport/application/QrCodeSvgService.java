package com.autoblog.publicreport.application;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class QrCodeSvgService {

    private static final int QR_SIZE = 256;

    public String createSvg(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    QR_SIZE,
                    QR_SIZE,
                    Map.of(EncodeHintType.MARGIN, 2)
            );
            return toSvg(matrix, content);
        } catch (WriterException exception) {
            throw new IllegalStateException("Unable to generate QR code", exception);
        }
    }

    private String toSvg(BitMatrix matrix, String content) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
                .append(matrix.getWidth())
                .append("\" height=\"")
                .append(matrix.getHeight())
                .append("\" viewBox=\"0 0 ")
                .append(matrix.getWidth())
                .append(' ')
                .append(matrix.getHeight())
                .append("\" role=\"img\">");
        svg.append("<title>AutoBlog public vehicle report QR code</title>");
        svg.append("<desc>").append(escapeXml(content)).append("</desc>");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#fff\"/>");
        svg.append("<path fill=\"#000\" d=\"");
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    svg.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
                }
            }
        }
        svg.append("\"/></svg>");
        return svg.toString();
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
