package com.leimo.imgcompress.utils;

import java.util.Arrays;

/**
 * Created by wangru
 * Date: 2018/6/5  19:43
 * mail: 1902065822@qq.com
 * describe:
 */

public enum FileType {

    Unknown("unKnown", Type.UNKNOWN, ""),
    //图片
    PNG("png", Type.IMAGE, "89504E", "89504E47"),
    JPG("jpg", Type.IMAGE, "FFD8FF", "FFD8FFE0", "FFD8FFE1", "FFD8FFE8"),
    GIF("gif", Type.IMAGE, "474946", "47494638", "89504E47"),
    TIFF("tiff", Type.IMAGE, "49492A00", "4D4D002A"),
    WEBP("webp", Type.IMAGE, "52494646"),
    BMP("bmp", Type.IMAGE, "424D", "424D46"),
    PSD("psd", Type.IMAGE, "38425053"),
    //
    WAV("wav", Type.AUDIO, "57415645"),
    AVI("avi", Type.AUDIO, "41564920"),
    MID("mid", Type.AUDIO, "4D546864"),
    //
    RM("rm", Type.VIDEO, "2E524D46"),
    MPG("mpg", Type.VIDEO, "000001BA", "000001B3"),
    MOV("mov", Type.VIDEO, "6D6F6F76"),
    ASF("asf", Type.VIDEO, "3026B2758E66CF11"),
    //
    RAR("rar", Type.COMPRESS, "526172", "52617221"),
    ZIP("zip", Type.COMPRESS, "504B03", "504B0304"),
    GZ("gz", Type.COMPRESS, "1F8B08"),
    //
    TXT("txt", Type.OTHER, "75736167"),
    RTF("rtf", Type.OTHER, "7B5C727466"), // 日记本
    DOCX("docx", Type.OTHER, "504B0304"),
    PDF("pdf", Type.OTHER, "255044462D312E"),
    DOC("doc", Type.OTHER, "D0CF11E0"),
    MDB("mdb", Type.OTHER, "5374616E64617264204A");


    interface Type {
        int UNKNOWN = 0;
        int IMAGE = 1;
        int AUDIO = 2;
        int VIDEO = 3;
        int COMPRESS = 4;
        int OTHER = 5;
    }

    FileType(String format, int type, String... header) {
        this.header = header;
        this.format = format;
        this.type = type;
    }

    String format;
    int type;
    String[] header;


    public static FileType getFileTypeByHeader(String header) {
        for (FileType c : FileType.values()) {
            if (c.getHeader() != null && Arrays.asList(c.getHeader()).contains(header)) {
                return c;
            }
        }
        return Unknown;
    }

    public boolean isImage() {
        return getType() == 1;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String[] getHeader() {
        return header;
    }

    public void setHeader(String[] header) {
        this.header = header;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
