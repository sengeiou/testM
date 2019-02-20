
package com.lemo.emojcenter.utils;

import android.support.v4.util.ArrayMap;

import com.lemo.emojcenter.R;


/**
 * @description :表情加载类,可自己添加多种表情，分别建立不同的map存放和不同的标志符即可
 */
public class EmotionUtils {

    /**
     * 表情类型标志符
     */
    public static final int EMOTION_CLASSIC_TYPE = 0x0001;//经典表情

    /**
     * key-表情文字;
     * value-表情图片资源
     */
    public static ArrayMap<String, Integer> EMPTY_MAP;
    public static ArrayMap<String, Integer> EMOTION_CLASSIC_MAP;


    static {
        EMPTY_MAP = new ArrayMap<>();
        EMOTION_CLASSIC_MAP = new ArrayMap<>();
        initData();
    }

    private static void initData() {
        String[] emojis = new String[] {
        EaseSmileUtils.ee_0,
        EaseSmileUtils.ee_1,
        EaseSmileUtils.ee_3,
        EaseSmileUtils.ee_4,
        EaseSmileUtils.ee_8,
        EaseSmileUtils.ee_9,

        EaseSmileUtils.ee_10,
        EaseSmileUtils.ee_11,
        EaseSmileUtils.ee_14,
        EaseSmileUtils.ee_18,

        EaseSmileUtils.ee_20,
        EaseSmileUtils.ee_23,
        EaseSmileUtils.ee_27,
        EaseSmileUtils.ee_28,

        EaseSmileUtils.ee_32,
        EaseSmileUtils.ee_33,
        EaseSmileUtils.ee_34,
        EaseSmileUtils.ee_35,
        EaseSmileUtils.ee_37,

        EaseSmileUtils.ee_40,
        EaseSmileUtils.ee_42,
        EaseSmileUtils.ee_45,
        EaseSmileUtils.ee_47,

        EaseSmileUtils.ee_51,
        EaseSmileUtils.ee_52,
        EaseSmileUtils.ee_54,
        EaseSmileUtils.ee_55,
        EaseSmileUtils.ee_56,
        EaseSmileUtils.ee_58,
        EaseSmileUtils.ee_59,

        EaseSmileUtils.ee_60,
        EaseSmileUtils.ee_64,
        EaseSmileUtils.ee_65,
        EaseSmileUtils.ee_66,
        EaseSmileUtils.ee_68,

        EaseSmileUtils.ee_73,
        EaseSmileUtils.ee_75,
        EaseSmileUtils.ee_76,
        EaseSmileUtils.ee_79,

        EaseSmileUtils.ee_82,
        EaseSmileUtils.ee_86,
        EaseSmileUtils.ee_87,
        EaseSmileUtils.ee_89,

        EaseSmileUtils.ee_90,
        EaseSmileUtils.ee_95,
        EaseSmileUtils.ee_97,
        EaseSmileUtils.ee_98,
        EaseSmileUtils.ee_99,

        EaseSmileUtils.ee_100,
        EaseSmileUtils.ee_101,
        EaseSmileUtils.ee_102,
        EaseSmileUtils.ee_103,
        EaseSmileUtils.ee_104,
        EaseSmileUtils.ee_105,
        EaseSmileUtils.ee_107,
        EaseSmileUtils.ee_108,

        EaseSmileUtils.ee_116,
        EaseSmileUtils.ee_117,

        EaseSmileUtils.ee_121,
        EaseSmileUtils.ee_122,
        EaseSmileUtils.ee_123,
        EaseSmileUtils.ee_124,
        EaseSmileUtils.ee_125,
        EaseSmileUtils.ee_126,
        EaseSmileUtils.ee_127,
        EaseSmileUtils.ee_128,
        EaseSmileUtils.ee_129,
        EaseSmileUtils.ee_131,
        };

        String[] emojis2 = new String[] {
        EaseSmileUtils.e_ok,
        EaseSmileUtils.e_loveyou,
        EaseSmileUtils.e_love,
        EaseSmileUtils.e_baoquan,
        EaseSmileUtils.e_money,
        EaseSmileUtils.e_ciya,
        EaseSmileUtils.e_cryloudly,
        EaseSmileUtils.e_cake,
        EaseSmileUtils.e_tram,
        EaseSmileUtils.e_rice,
        EaseSmileUtils.e_airplane,
        EaseSmileUtils.e_kiss,
        EaseSmileUtils.e_struggle,
        EaseSmileUtils.e_highcar,
        EaseSmileUtils.e_applause,
        EaseSmileUtils.e_yawn,
        EaseSmileUtils.e_shy,
        EaseSmileUtils.e_sillysmile,
        EaseSmileUtils.e_cheer,
        EaseSmileUtils.e_waving,
        EaseSmileUtils.e_lookback,
        EaseSmileUtils.e_train,
        EaseSmileUtils.e_excitement,
        EaseSmileUtils.e_hiphop,
        EaseSmileUtils.e_surprise,
        EaseSmileUtils.e_coffee,
        EaseSmileUtils.e_kowtow,
        EaseSmileUtils.e_cute,
        EaseSmileUtils.e_poor,
        EaseSmileUtils.e_crying,
        EaseSmileUtils.e_sleepy,
        EaseSmileUtils.e_candle,
        EaseSmileUtils.e_coldsweat,
        EaseSmileUtils.e_gift,
        EaseSmileUtils.e_tears,
        EaseSmileUtils.e_noodle,
        EaseSmileUtils.e_sad,
        EaseSmileUtils.e_mouth,
        EaseSmileUtils.e_car,
        EaseSmileUtils.e_good,
        EaseSmileUtils.e_embarrassed,
        EaseSmileUtils.e_fist,
        EaseSmileUtils.e_victory,
        EaseSmileUtils.e_doublehappiness,
        EaseSmileUtils.e_sleep,
        EaseSmileUtils.e_flowers,
        EaseSmileUtils.e_sun,
        EaseSmileUtils.e_naughty,
        EaseSmileUtils.e_skipping,
        EaseSmileUtils.e_jump,
        EaseSmileUtils.e_laughter,
        EaseSmileUtils.e_smile,
        EaseSmileUtils.e_grievances,
        EaseSmileUtils.e_handshake,
        EaseSmileUtils.e_watermelon,
        EaseSmileUtils.e_rain,
        EaseSmileUtils.e_banana,
        EaseSmileUtils.e_shh,
        EaseSmileUtils.e_doubt,
        EaseSmileUtils.e_cloudy,
        EaseSmileUtils.e_hug,
        EaseSmileUtils.e_righttaiji,
        EaseSmileUtils.e_moon,
        EaseSmileUtils.e_sports,
        EaseSmileUtils.e_bye,
        EaseSmileUtils.e_fortunecat,
        EaseSmileUtils.e_circle,
        EaseSmileUtils.e_lefttaiji,
        };

        int[] icons = new int[] {
        R.drawable.f_static_000,
        R.drawable.f_static_001,
        R.drawable.f_static_003,
        R.drawable.f_static_004,
        R.drawable.f_static_008,
        R.drawable.f_static_009,
        R.drawable.f_static_010,
        R.drawable.f_static_011,
        R.drawable.f_static_014,
        R.drawable.f_static_018,
        R.drawable.f_static_020,
        R.drawable.f_static_023,
        R.drawable.f_static_027,
        R.drawable.f_static_028,
        R.drawable.f_static_032,
        R.drawable.f_static_033,
        R.drawable.f_static_034,
        R.drawable.f_static_035,
        R.drawable.f_static_037,
        R.drawable.f_static_040,
        R.drawable.f_static_042,
        R.drawable.f_static_045,
        R.drawable.f_static_047,
        R.drawable.f_static_051,
        R.drawable.f_static_052,
        R.drawable.f_static_054,
        R.drawable.f_static_055,
        R.drawable.f_static_056,
        R.drawable.f_static_058,
        R.drawable.f_static_059,
        R.drawable.f_static_060,
        R.drawable.f_static_064,
        R.drawable.f_static_065,
        R.drawable.f_static_066,
        R.drawable.f_static_068,
        R.drawable.f_static_073,
        R.drawable.f_static_075,
        R.drawable.f_static_076,
        R.drawable.f_static_079,
        R.drawable.f_static_082,
        R.drawable.f_static_086,
        R.drawable.f_static_087,
        R.drawable.f_static_089,
        R.drawable.f_static_090,
        R.drawable.f_static_095,
        R.drawable.f_static_097,
        R.drawable.f_static_098,
        R.drawable.f_static_099,
        R.drawable.f_static_100,
        R.drawable.f_static_101,
        R.drawable.f_static_102,
        R.drawable.f_static_103,
        R.drawable.f_static_104,
        R.drawable.f_static_105,
        R.drawable.f_static_107,
        R.drawable.f_static_108,
        R.drawable.f_static_116,
        R.drawable.f_static_117,
        R.drawable.f_static_121,
        R.drawable.f_static_122,
        R.drawable.f_static_123,
        R.drawable.f_static_124,
        R.drawable.f_static_125,
        R.drawable.f_static_126,
        R.drawable.f_static_127,
        R.drawable.f_static_128,
        R.drawable.f_static_129,
        R.drawable.f_static_131,
        };

        int[] icons2 = new int[] {
        R.drawable.ok,
        R.drawable.loveyou,
        R.drawable.love,
        R.drawable.baoquan,
        R.drawable.money,
        R.drawable.ciya,
        R.drawable.cryloudly,
        R.drawable.cake,
        R.drawable.tram,
        R.drawable.rice,
        R.drawable.airplane,
        R.drawable.kiss,
        R.drawable.struggle,
        R.drawable.highcar,
        R.drawable.applause,
        R.drawable.yawn,
        R.drawable.shy,
        R.drawable.sillysmile,
        R.drawable.cheer,
        R.drawable.waving,
        R.drawable.lookback,
        R.drawable.train,
        R.drawable.excitement,
        R.drawable.hiphop,
        R.drawable.surprise,
        R.drawable.coffee,
        R.drawable.kowtow,
        R.drawable.cute,
        R.drawable.poor,
        R.drawable.crying,
        R.drawable.sleepy,
        R.drawable.candle,
        R.drawable.coldsweat,
        R.drawable.gift,
        R.drawable.tears,
        R.drawable.noodle,
        R.drawable.sad,
        R.drawable.mouth,
        R.drawable.car,
        R.drawable.good,
        R.drawable.embarrassed,
        R.drawable.fist,
        R.drawable.victory,
        R.drawable.doublehappiness,
        R.drawable.sleep,
        R.drawable.flowers,
        R.drawable.sun,
        R.drawable.naughty,
        R.drawable.skipping,
        R.drawable.jump,
        R.drawable.laughter,
        R.drawable.smile,
        R.drawable.grievances,
        R.drawable.handshake,
        R.drawable.watermelon,
        R.drawable.rain,
        R.drawable.banana,
        R.drawable.shh,
        R.drawable.doubt,
        R.drawable.cloudy,
        R.drawable.hug,
        R.drawable.righttaiji,
        R.drawable.moon,
        R.drawable.sports,
        R.drawable.bye,
        R.drawable.fortunecat,
        R.drawable.circle,
        R.drawable.lefttaiji,
        };

        for (int i = 0; i < icons.length; i++) {
//            EMOTION_CLASSIC_MAP.put(emojis[i], icons[i]);
            EMOTION_CLASSIC_MAP.put(emojis2[i], icons2[i]);
        }
    }

    //    private static void initData() {
    //        EMOTION_CLASSIC_MAP.put("[呵呵]", R.drawable.d_hehe);
    //        EMOTION_CLASSIC_MAP.put("[嘻嘻]", R.drawable.d_xixi);
    //        EMOTION_CLASSIC_MAP.put("[哈哈]", R.drawable.d_haha);
    //        EMOTION_CLASSIC_MAP.put("[爱你]", R.drawable.d_aini);
    //        EMOTION_CLASSIC_MAP.put("[挖鼻屎]", R.drawable.d_wabishi);
    //        EMOTION_CLASSIC_MAP.put("[吃惊]", R.drawable.d_chijing);
    //        EMOTION_CLASSIC_MAP.put("[晕]", R.drawable.d_yun);
    //        EMOTION_CLASSIC_MAP.put("[泪]", R.drawable.d_lei);
    //        EMOTION_CLASSIC_MAP.put("[馋嘴]", R.drawable.d_chanzui);
    //        EMOTION_CLASSIC_MAP.put("[抓狂]", R.drawable.d_zhuakuang);
    //        EMOTION_CLASSIC_MAP.put("[哼]", R.drawable.d_heng);
    //        EMOTION_CLASSIC_MAP.put("[可爱]", R.drawable.d_keai);
    //        EMOTION_CLASSIC_MAP.put("[怒]", R.drawable.d_nu);
    //        EMOTION_CLASSIC_MAP.put("[汗]", R.drawable.d_han);
    //        EMOTION_CLASSIC_MAP.put("[害羞]", R.drawable.d_haixiu);
    //        EMOTION_CLASSIC_MAP.put("[睡觉]", R.drawable.d_shuijiao);
    //        EMOTION_CLASSIC_MAP.put("[钱]", R.drawable.d_qian);
    //        EMOTION_CLASSIC_MAP.put("[偷笑]", R.drawable.d_touxiao);
    //        EMOTION_CLASSIC_MAP.put("[笑cry]", R.drawable.d_xiaoku);
    //        EMOTION_CLASSIC_MAP.put("[doge]", R.drawable.d_doge);
    //        EMOTION_CLASSIC_MAP.put("[喵喵]", R.drawable.d_miao);
    //        EMOTION_CLASSIC_MAP.put("[酷]", R.drawable.d_ku);
    //        EMOTION_CLASSIC_MAP.put("[衰]", R.drawable.d_shuai);
    //        EMOTION_CLASSIC_MAP.put("[闭嘴]", R.drawable.d_bizui);
    //        EMOTION_CLASSIC_MAP.put("[鄙视]", R.drawable.d_bishi);
    //        EMOTION_CLASSIC_MAP.put("[花心]", R.drawable.d_huaxin);
    //        EMOTION_CLASSIC_MAP.put("[鼓掌]", R.drawable.d_guzhang);
    //        EMOTION_CLASSIC_MAP.put("[悲伤]", R.drawable.d_beishang);
    //        EMOTION_CLASSIC_MAP.put("[思考]", R.drawable.d_sikao);
    //        EMOTION_CLASSIC_MAP.put("[生病]", R.drawable.d_shengbing);
    //        EMOTION_CLASSIC_MAP.put("[亲亲]", R.drawable.d_qinqin);
    //        EMOTION_CLASSIC_MAP.put("[怒骂]", R.drawable.d_numa);
    //        EMOTION_CLASSIC_MAP.put("[太开心]", R.drawable.d_taikaixin);
    //        EMOTION_CLASSIC_MAP.put("[懒得理你]", R.drawable.d_landelini);
    //        EMOTION_CLASSIC_MAP.put("[右哼哼]", R.drawable.d_youhengheng);
    //        EMOTION_CLASSIC_MAP.put("[左哼哼]", R.drawable.d_zuohengheng);
    //        EMOTION_CLASSIC_MAP.put("[嘘]", R.drawable.d_xu);
    //        EMOTION_CLASSIC_MAP.put("[委屈]", R.drawable.d_weiqu);
    //        EMOTION_CLASSIC_MAP.put("[吐]", R.drawable.d_tu);
    //        EMOTION_CLASSIC_MAP.put("[可怜]", R.drawable.d_kelian);
    //        EMOTION_CLASSIC_MAP.put("[打哈气]", R.drawable.d_dahaqi);
    //        EMOTION_CLASSIC_MAP.put("[挤眼]", R.drawable.d_jiyan);
    //        EMOTION_CLASSIC_MAP.put("[失望]", R.drawable.d_shiwang);
    //        EMOTION_CLASSIC_MAP.put("[顶]", R.drawable.d_ding);
    //        EMOTION_CLASSIC_MAP.put("[疑问]", R.drawable.d_yiwen);
    //        EMOTION_CLASSIC_MAP.put("[困]", R.drawable.d_kun);
    //        EMOTION_CLASSIC_MAP.put("[感冒]", R.drawable.d_ganmao);
    //        EMOTION_CLASSIC_MAP.put("[拜拜]", R.drawable.d_baibai);
    //        EMOTION_CLASSIC_MAP.put("[黑线]", R.drawable.d_heixian);
    //        EMOTION_CLASSIC_MAP.put("[阴险]", R.drawable.d_yinxian);
    //        EMOTION_CLASSIC_MAP.put("[打脸]", R.drawable.d_dalian);
    //        EMOTION_CLASSIC_MAP.put("[傻眼]", R.drawable.d_shayan);
    //        EMOTION_CLASSIC_MAP.put("[猪头]", R.drawable.d_zhutou);
    //        EMOTION_CLASSIC_MAP.put("[熊猫]", R.drawable.d_xiongmao);
    //        EMOTION_CLASSIC_MAP.put("[兔子]", R.drawable.d_tuzi);
    //    }


    /**
     * 根据名称获取当前表情图标R值
     *
     * @param EmotionType 表情类型标志符
     * @param imgName     名称
     * @return
     */
    public static int getImgByName(int EmotionType, String imgName) {
        Integer integer = null;
        switch (EmotionType) {
            case EMOTION_CLASSIC_TYPE:
                integer = EMOTION_CLASSIC_MAP.get(imgName);
                break;
            default:
                break;
        }
        return integer == null ? -1 : integer;
    }

    /**
     * 根据类型获取表情数据
     *
     * @param EmotionType
     * @return
     */
    public static ArrayMap<String, Integer> getEmojiMap(int EmotionType) {
        ArrayMap EmojiMap = new ArrayMap();
        switch (EmotionType) {
            case EMOTION_CLASSIC_TYPE:
                EmojiMap = EMOTION_CLASSIC_MAP;
                break;
            default:
                EmojiMap = EMPTY_MAP;
                break;
        }
        return EmojiMap;
    }
}
