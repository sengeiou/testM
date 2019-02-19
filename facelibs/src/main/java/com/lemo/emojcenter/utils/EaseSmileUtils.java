package com.lemo.emojcenter.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.lemo.emojcenter.bean.EmojBean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangru
 * Date: 2018/2/28  10:51
 * mail: 1902065822@qq.com
 * describe:
 */

public class EaseSmileUtils {
    public static final String ee_0 = "[000]";
    public static final String ee_1 = "[001]";
    public static final String ee_2 = "[002]";
    public static final String ee_3 = "[003]";
    public static final String ee_4 = "[004]";
    public static final String ee_5 = "[005]";
    public static final String ee_6 = "[006]";
    public static final String ee_7 = "[007]";
    public static final String ee_8 = "[008]";
    public static final String ee_9 = "[009]";
    public static final String ee_10 = "[010]";
    public static final String ee_11 = "[011]";
    public static final String ee_12 = "[012]";
    public static final String ee_13 = "[013]";
    public static final String ee_14 = "[014]";
    public static final String ee_15 = "[015]";
    public static final String ee_16 = "[016]";
    public static final String ee_17 = "[017]";
    public static final String ee_18 = "[018]";
    public static final String ee_19 = "[019]";
    public static final String ee_20 = "[020]";
    public static final String ee_21 = "[021]";
    public static final String ee_22 = "[022]";
    public static final String ee_23 = "[023]";
    public static final String ee_24 = "[024]";
    public static final String ee_25 = "[025]";
    public static final String ee_26 = "[026]";
    public static final String ee_27 = "[027]";
    public static final String ee_28 = "[028]";
    public static final String ee_29 = "[029]";
    public static final String ee_30 = "[030]";
    public static final String ee_31 = "[031]";
    public static final String ee_32 = "[032]";
    public static final String ee_33 = "[033]";
    public static final String ee_34 = "[034]";
    public static final String ee_35 = "[035]";
    public static final String ee_36 = "[036]";
    public static final String ee_37 = "[037]";
    public static final String ee_38 = "[038]";
    public static final String ee_39 = "[039]";
    public static final String ee_40 = "[040]";
    public static final String ee_41 = "[041]";
    public static final String ee_42 = "[042]";
    public static final String ee_43 = "[043]";
    public static final String ee_44 = "[044]";
    public static final String ee_45 = "[045]";
    public static final String ee_46 = "[046]";
    public static final String ee_47 = "[047]";
    public static final String ee_48 = "[048]";
    public static final String ee_49 = "[049]";
    public static final String ee_50 = "[050]";
    public static final String ee_51 = "[051]";
    public static final String ee_52 = "[052]";
    public static final String ee_53 = "[053]";
    public static final String ee_54 = "[054]";
    public static final String ee_55 = "[055]";
    public static final String ee_56 = "[056]";
    public static final String ee_57 = "[057]";
    public static final String ee_58 = "[058]";
    public static final String ee_59 = "[059]";
    public static final String ee_60 = "[060]";
    public static final String ee_61 = "[061]";
    public static final String ee_62 = "[062]";
    public static final String ee_63 = "[063]";
    public static final String ee_64 = "[064]";
    public static final String ee_65 = "[065]";
    public static final String ee_66 = "[066]";
    public static final String ee_67 = "[067]";
    public static final String ee_68 = "[068]";
    public static final String ee_69 = "[069]";
    public static final String ee_70 = "[070]";
    public static final String ee_71 = "[071]";
    public static final String ee_72 = "[072]";
    public static final String ee_73 = "[073]";
    public static final String ee_74 = "[074]";
    public static final String ee_75 = "[075]";
    public static final String ee_76 = "[076]";
    public static final String ee_77 = "[077]";
    public static final String ee_78 = "[078]";
    public static final String ee_79 = "[079]";
    public static final String ee_80 = "[080]";
    public static final String ee_81 = "[081]";
    public static final String ee_82 = "[082]";
    public static final String ee_83 = "[083]";
    public static final String ee_84 = "[084]";
    public static final String ee_85 = "[085]";
    public static final String ee_86 = "[086]";
    public static final String ee_87 = "[087]";
    public static final String ee_88 = "[088]";
    public static final String ee_89 = "[089]";
    public static final String ee_90 = "[090]";
    public static final String ee_91 = "[091]";
    public static final String ee_92 = "[092]";
    public static final String ee_93 = "[093]";
    public static final String ee_94 = "[094]";
    public static final String ee_95 = "[095]";
    public static final String ee_96 = "[096]";
    public static final String ee_97 = "[097]";
    public static final String ee_98 = "[098]";
    public static final String ee_99 = "[099]";
    public static final String ee_100 = "[100]";
    public static final String ee_101 = "[101]";
    public static final String ee_102 = "[102]";
    public static final String ee_103 = "[103]";
    public static final String ee_104 = "[104]";
    public static final String ee_105 = "[105]";
    public static final String ee_106 = "[106]";
    public static final String ee_107 = "[107]";
    public static final String ee_108 = "[108]";
    public static final String ee_109 = "[109]";
    public static final String ee_110 = "[110]";
    public static final String ee_111 = "[111]";
    public static final String ee_112 = "[112]";
    public static final String ee_113 = "[113]";
    public static final String ee_114 = "[114]";
    public static final String ee_115 = "[115]";
    public static final String ee_116 = "[116]";
    public static final String ee_117 = "[117]";
    public static final String ee_118 = "[118]";
    public static final String ee_119 = "[119]";
    public static final String ee_120 = "[120]";
    public static final String ee_121 = "[121]";
    public static final String ee_122 = "[122]";
    public static final String ee_123 = "[123]";
    public static final String ee_124 = "[124]";
    public static final String ee_125 = "[125]";
    public static final String ee_126 = "[126]";
    public static final String ee_127 = "[127]";
    public static final String ee_128 = "[128]";
    public static final String ee_129 = "[129]";
    public static final String ee_130 = "[130]";
    public static final String ee_131 = "[131]";
    public static final String ee_132 = "[132]";
    public static final String ee_133 = "[133]";
    public static final String ee_134 = "[134]";
    public static final String ee_135 = "[135]";
    public static final String ee_136 = "[136]";
    public static final String ee_137 = "[137]";
    public static final String ee_138 = "[138]";
    public static final String ee_139 = "[139]";
    public static final String ee_140 = "[140]";
    public static final String ee_141 = "[141]";
    public static final String ee_142 = "[142]";

    public static final String f_static_143 = "[143]";
    public static final String f_static_144 = "[144]";
    public static final String f_static_145 = "[145]";
    public static final String f_static_146 = "[146]";
    public static final String f_static_147 = "[147]";
    public static final String f_static_148 = "[148]";
    public static final String f_static_149 = "[149]";
    public static final String f_static_150 = "[150]";
    public static final String f_static_151 = "[151]";
    public static final String f_static_152 = "[152]";
    public static final String f_static_153 = "[153]";
    public static final String f_static_154 = "[154]";
    public static final String f_static_155 = "[155]";
    public static final String f_static_156 = "[156]";
    public static final String f_static_157 = "[157]";
    public static final String f_static_158 = "[158]";
    public static final String f_static_159 = "[159]";
    public static final String f_static_160 = "[160]";
    public static final String f_static_161 = "[161]";
    public static final String f_static_162 = "[162]";
    public static final String f_static_163 = "[163]";
    public static final String f_static_164 = "[164]";
    public static final String f_static_165 = "[165]";
    public static final String f_static_166 = "[166]";

    public static final String e_ok = "[OK]";
    public static final String e_loveyou = "[爱你]";
    public static final String e_love = "[爱心]";
    public static final String e_baoquan = "[抱拳]";
    public static final String e_money = "[财富]";
    public static final String e_ciya = "[呲牙]";
    public static final String e_cryloudly = "[大哭]";
    public static final String e_cake = "[蛋糕]";
    public static final String e_tram = "[电车]";
    public static final String e_rice = "[饭]";
    public static final String e_airplane = "[飞机]";
    public static final String e_kiss = "[飞吻]";
    public static final String e_struggle = "[奋斗]";
    public static final String e_highcar = "[高铁]";
    public static final String e_applause = "[鼓掌]";
    public static final String e_yawn = "[哈欠]";
    public static final String e_shy = "[害羞]";
    public static final String e_sillysmile = "[憨笑]";
    public static final String e_cheer = "[喝彩]";
    public static final String e_waving = "[挥手]";
    public static final String e_lookback = "[回头]";
    public static final String e_train = "[火车]";
    public static final String e_excitement = "[激动]";
    public static final String e_hiphop = "[街舞]";
    public static final String e_surprise = "[惊讶]";
    public static final String e_coffee = "[咖啡]";
    public static final String e_kowtow = "[磕头]";
    public static final String e_cute = "[可爱]";
    public static final String e_poor = "[可怜]";
    public static final String e_crying = "[快哭了]";
    public static final String e_sleepy = "[困]";
    public static final String e_candle = "[蜡烛]";
    public static final String e_coldsweat = "[冷汗]";
    public static final String e_gift = "[礼物]";
    public static final String e_tears = "[流泪]";
    public static final String e_noodle = "[面]";
    public static final String e_sad = "[难过]";
    public static final String e_mouth = "[撇嘴]";
    public static final String e_car = "[汽车]";
    public static final String e_good = "[强]";
    public static final String e_embarrassed = "[糗大了]";
    public static final String e_fist = "[拳头]";
    public static final String e_victory = "[胜利]";
    public static final String e_doublehappiness = "[双喜]";
    public static final String e_sleep = "[睡]";
    public static final String e_flowers = "[送花]";
    public static final String e_sun = "[太阳]";
    public static final String e_naughty = "[调皮]";
    public static final String e_skipping = "[跳绳]";
    public static final String e_jump = "[跳跳]";
    public static final String e_laughter = "[偷笑]";
    public static final String e_smile = "[微笑]";
    public static final String e_grievances = "[委屈]";
    public static final String e_handshake = "[握手]";
    public static final String e_watermelon = "[西瓜]";
    public static final String e_rain = "[下雨]";
    public static final String e_banana = "[香蕉]";
    public static final String e_shh = "[嘘]";
    public static final String e_doubt = "[疑问]";
    public static final String e_cloudy = "[阴天]";
    public static final String e_hug = "[拥抱]";
    public static final String e_righttaiji = "[右太极]";
    public static final String e_moon = "[月亮]";
    public static final String e_sports = "[运动]";
    public static final String e_bye = "[再见]";
    public static final String e_fortunecat = "[招财猫]";
    public static final String e_circle = "[转圈]";
    public static final String e_lefttaiji = "[左太极]";

    private static final Spannable.Factory spannableFactory = Spannable.Factory
    .getInstance();
    private static final Map<Pattern, Object> emoticons = new HashMap<Pattern, Object>();
    @SuppressLint("UseSparseArrays")
    private static Map<Integer,Drawable> drawables = new HashMap<>();

    static {
        ArrayMap<String, Integer> emotionMap = EmotionUtils.getEmojiMap(EmotionUtils.EMOTION_CLASSIC_TYPE);
        for (int i = 0; i < emotionMap.size(); i++) {
            String content = emotionMap.keyAt(i);
            Integer resId = emotionMap.valueAt(i);
            EmojBean emojBean = new EmojBean(content, resId);
            addPattern(content, resId);
        }
    }

    /**
     * add text and icon to the map
     * @param emojiText -- text of emoji
     * @param icon -- resource id or local path
     */
    private static void addPattern(String emojiText, Object icon) {
        emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
    }


    /**
     * replace existing spannable with smiles
     * @param context
     * @param spannable
     * @return
     */
    public static boolean addSmiles(Context context, Spannable spannable, int type) {
        boolean hasChanges = false;
        for (Map.Entry<Pattern, Object> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(),
                matcher.end(), ImageSpan.class)) {
                    if (spannable.getSpanStart(span) >= matcher.start()
                    && spannable.getSpanEnd(span) <= matcher.end()) {
                        spannable.removeSpan(span);
                    } else {
                        set = false;
                        break;
                    }
                }
                if (set) {
                    hasChanges = true;
                    Object value = entry.getValue();
                    if (value instanceof String && !((String) value).startsWith("http")) {
                        File file = new File((String) value);
                        if (!file.exists() || file.isDirectory()) {
                            return false;
                        }
                        spannable.setSpan(new ImageSpan(context, Uri.fromFile(file)),
                        matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        //复用drawable
                        Drawable drawable;
                        if (drawables.containsKey(value)){
                            drawable = drawables.get(value);
                        }else{
                            drawable = context.getResources().getDrawable((Integer) value);
                            drawables.put((Integer) value,drawable);
                        }
                        float density = context.getResources().getDisplayMetrics().density;
                        drawable.setBounds(0, 0, (int) (26 * density), (int) (26 * density));
                        ImageSpan span;
                        if (type == 1) {
                            span = new ImageSpan(drawable);
                        } else {
                            span = new ImageSpan(context, (Integer) value);
                        }
                        spannable.setSpan(span,
                        matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

        return hasChanges;
    }

    public static Spannable getSmiledText(Context context, CharSequence text, int type) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable, type);
        return spannable;
    }

    public static boolean containsKey(String key) {
        boolean b = false;
        for (Map.Entry<Pattern, Object> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(key);
            if (matcher.find()) {
                b = true;
                break;
            }
        }

        return b;
    }

    public static int getSmilesSize() {
        return emoticons.size();
    }
}
