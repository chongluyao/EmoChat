package com.zhouqing.EmoChat.main.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    //写字符串到指定路径
    public static void writeStrToPath(String fileContent,String path)
    {
        String outputFileName = "chatContent_"+ System.currentTimeMillis()+".txt";
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        FileOutputStream fos= null;
        try {
            fos = new FileOutputStream(path+outputFileName);
            fos.write(fileContent.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
