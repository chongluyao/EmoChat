package com.zhouqing.EmoChat.common.util;


import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;


public class PinyinUtil {
    public static String getPinyin(String name){
        return PinyinHelper.convertToPinyinString(name,"", PinyinFormat.WITHOUT_TONE);
    }
}
