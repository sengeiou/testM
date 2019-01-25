package com.app.common.utils;

import android.text.TextUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Locale;

public class HanziToPinyin {
    private static final char[] UNIHANS = new char[] {'阿', '哎', '安', '肮', '凹', '八', '挀', '扳', '邦', '勹', '陂', '奔', '伻', '屄', '边', '灬', '憋', '汃', '冫', '癶', '峬', '嚓', '偲', '参', '仓', '撡', '冊', '嵾', '曽', '曾', '層', '叉', '芆', '辿', '伥', '抄', '车', '抻', '沈', '沉', '阷', '吃', '充', '抽', '出', '欻', '揣', '巛', '刅', '吹', '旾', '逴', '呲', '匆', '凑', '粗', '汆', '崔', '邨', '搓', '咑', '呆', '丹', '当', '刀', '嘚', '扥', '灯', '氐', '嗲', '甸', '刁', '爹', '丁', '丟', '东', '吺', '厾', '耑', '襨', '吨', '多', '妸', '诶', '奀', '鞥', '儿', '发', '帆', '匚', '飞', '分', '丰', '覅', '仏', '紑', '伕', '旮', '侅', '甘', '冈', '皋', '戈', '给', '根', '刯', '工', '勾', '估', '瓜', '乖', '关', '光', '归', '丨', '呙', '哈', '咍', '佄', '夯', '茠', '诃', '黒', '拫', '亨', '噷', '叿', '齁', '乯', '花', '怀', '犿', '巟', '灰', '昏', '吙', '丌', '加', '戋', '江', '艽', '阶', '巾', '坕', '冂', '丩', '凥', '姢', '噘', '军', '咔', '开', '刊', '忼', '尻', '匼', '肎', '劥', '空', '抠', '扝', '夸', '蒯', '宽', '匡', '亏', '坤', '扩', '垃', '来', '兰', '啷', '捞', '肋', '勒', '崚', '刕', '俩', '奁', '良', '撩', '列', '拎', '刢', '溜', '囖', '龙', '瞜', '噜', '娈', '畧', '抡', '罗', '呣', '妈', '埋', '嫚', '牤', '猫', '么', '呅', '门', '甿', '咪', '宀', '喵', '乜', '民', '名', '谬', '摸', '哞', '毪', '嗯', '拏', '腉', '囡', '囔', '孬', '疒', '娞', '恁', '能', '妮', '拈', '嬢', '鸟', '捏', '囜', '宁', '妞', '农', '羺', '奴', '奻', '疟', '黁', '郍', '喔', '讴', '妑', '拍', '眅', '乓', '抛', '呸', '喷', '匉', '丕', '囨', '剽', '氕', '姘', '乒', '钋', '剖', '仆', '七', '掐', '千', '呛', '悄', '癿', '亲', '狅', '芎', '丘', '区', '峑', '缺', '夋', '呥', '穣', '娆', '惹', '人', '扔', '日', '茸', '厹', '邚', '挼', '堧', '婑', '瞤', '捼', '仨', '毢', '三', '桒', '掻', '閪', '森', '僧', '杀', '筛', '山', '伤', '弰', '奢', '申', '莘', '敒', '升', '尸', '収', '书', '刷', '衰', '闩', '双', '谁', '吮', '说', '厶', '忪', '捜', '苏', '狻', '夊', '孙', '唆', '他', '囼', '坍', '汤', '夲', '忑', '熥', '剔', '天', '旫', '帖', '厅', '囲', '偷', '凸', '湍', '推', '吞', '乇', '穵', '歪', '弯', '尣', '危', '昷', '翁', '挝', '乌', '夕', '虲', '仚', '乡', '灱', '些', '心', '星', '凶', '休', '吁', '吅', '削', '坃', '丫', '恹', '央', '幺', '倻', '一', '囙', '应', '哟', '佣', '优', '扜', '囦', '曰', '晕', '筠', '筼', '帀', '災', '兂', '匨', '傮', '则', '贼', '怎', '増', '扎', '捚', '沾', '张', '长', '長', '佋', '蜇', '贞', '争', '之', '峙', '庢', '中', '州', '朱', '抓', '拽', '专', '妆', '隹', '宒', '卓', '乲', '宗', '邹', '租', '钻', '厜', '尊', '昨', '兙', '鿃', '鿄'};
    private static final byte[][] PINYINS = new byte[][] {{65, 0, 0, 0, 0, 0}, {65, 73, 0, 0, 0, 0}, {65, 78, 0, 0, 0, 0}, {65, 78, 71, 0, 0, 0}, {65, 79, 0, 0, 0, 0}, {66, 65, 0, 0, 0, 0}, {66, 65, 73, 0, 0, 0}, {66, 65, 78, 0, 0, 0}, {66, 65, 78, 71, 0, 0}, {66, 65, 79, 0, 0, 0}, {66, 69, 73, 0, 0, 0}, {66, 69, 78, 0, 0, 0}, {66, 69, 78, 71, 0, 0}, {66, 73, 0, 0, 0, 0}, {66, 73, 65, 78, 0, 0}, {66, 73, 65, 79, 0, 0}, {66, 73, 69, 0, 0, 0}, {66, 73, 78, 0, 0, 0}, {66, 73, 78, 71, 0, 0}, {66, 79, 0, 0, 0, 0}, {66, 85, 0, 0, 0, 0}, {67, 65, 0, 0, 0, 0}, {67, 65, 73, 0, 0, 0}, {67, 65, 78, 0, 0, 0}, {67, 65, 78, 71, 0, 0}, {67, 65, 79, 0, 0, 0}, {67, 69, 0, 0, 0, 0}, {67, 69, 78, 0, 0, 0}, {67, 69, 78, 71, 0, 0}, {90, 69, 78, 71, 0, 0}, {67, 69, 78, 71, 0, 0}, {67, 72, 65, 0, 0, 0}, {67, 72, 65, 73, 0, 0}, {67, 72, 65, 78, 0, 0}, {67, 72, 65, 78, 71, 0}, {67, 72, 65, 79, 0, 0}, {67, 72, 69, 0, 0, 0}, {67, 72, 69, 78, 0, 0}, {83, 72, 69, 78, 0, 0}, {67, 72, 69, 78, 0, 0}, {67, 72, 69, 78, 71, 0}, {67, 72, 73, 0, 0, 0}, {67, 72, 79, 78, 71, 0}, {67, 72, 79, 85, 0, 0}, {67, 72, 85, 0, 0, 0}, {67, 72, 85, 65, 0, 0}, {67, 72, 85, 65, 73, 0}, {67, 72, 85, 65, 78, 0}, {67, 72, 85, 65, 78, 71}, {67, 72, 85, 73, 0, 0}, {67, 72, 85, 78, 0, 0}, {67, 72, 85, 79, 0, 0}, {67, 73, 0, 0, 0, 0}, {67, 79, 78, 71, 0, 0}, {67, 79, 85, 0, 0, 0}, {67, 85, 0, 0, 0, 0}, {67, 85, 65, 78, 0, 0}, {67, 85, 73, 0, 0, 0}, {67, 85, 78, 0, 0, 0}, {67, 85, 79, 0, 0, 0}, {68, 65, 0, 0, 0, 0}, {68, 65, 73, 0, 0, 0}, {68, 65, 78, 0, 0, 0}, {68, 65, 78, 71, 0, 0}, {68, 65, 79, 0, 0, 0}, {68, 69, 0, 0, 0, 0}, {68, 69, 78, 0, 0, 0}, {68, 69, 78, 71, 0, 0}, {68, 73, 0, 0, 0, 0}, {68, 73, 65, 0, 0, 0}, {68, 73, 65, 78, 0, 0}, {68, 73, 65, 79, 0, 0}, {68, 73, 69, 0, 0, 0}, {68, 73, 78, 71, 0, 0}, {68, 73, 85, 0, 0, 0}, {68, 79, 78, 71, 0, 0}, {68, 79, 85, 0, 0, 0}, {68, 85, 0, 0, 0, 0}, {68, 85, 65, 78, 0, 0}, {68, 85, 73, 0, 0, 0}, {68, 85, 78, 0, 0, 0}, {68, 85, 79, 0, 0, 0}, {69, 0, 0, 0, 0, 0}, {69, 73, 0, 0, 0, 0}, {69, 78, 0, 0, 0, 0}, {69, 78, 71, 0, 0, 0}, {69, 82, 0, 0, 0, 0}, {70, 65, 0, 0, 0, 0}, {70, 65, 78, 0, 0, 0}, {70, 65, 78, 71, 0, 0}, {70, 69, 73, 0, 0, 0}, {70, 69, 78, 0, 0, 0}, {70, 69, 78, 71, 0, 0}, {70, 73, 65, 79, 0, 0}, {70, 79, 0, 0, 0, 0}, {70, 79, 85, 0, 0, 0}, {70, 85, 0, 0, 0, 0}, {71, 65, 0, 0, 0, 0}, {71, 65, 73, 0, 0, 0}, {71, 65, 78, 0, 0, 0}, {71, 65, 78, 71, 0, 0}, {71, 65, 79, 0, 0, 0}, {71, 69, 0, 0, 0, 0}, {71, 69, 73, 0, 0, 0}, {71, 69, 78, 0, 0, 0}, {71, 69, 78, 71, 0, 0}, {71, 79, 78, 71, 0, 0}, {71, 79, 85, 0, 0, 0}, {71, 85, 0, 0, 0, 0}, {71, 85, 65, 0, 0, 0}, {71, 85, 65, 73, 0, 0}, {71, 85, 65, 78, 0, 0}, {71, 85, 65, 78, 71, 0}, {71, 85, 73, 0, 0, 0}, {71, 85, 78, 0, 0, 0}, {71, 85, 79, 0, 0, 0}, {72, 65, 0, 0, 0, 0}, {72, 65, 73, 0, 0, 0}, {72, 65, 78, 0, 0, 0}, {72, 65, 78, 71, 0, 0}, {72, 65, 79, 0, 0, 0}, {72, 69, 0, 0, 0, 0}, {72, 69, 73, 0, 0, 0}, {72, 69, 78, 0, 0, 0}, {72, 69, 78, 71, 0, 0}, {72, 77, 0, 0, 0, 0}, {72, 79, 78, 71, 0, 0}, {72, 79, 85, 0, 0, 0}, {72, 85, 0, 0, 0, 0}, {72, 85, 65, 0, 0, 0}, {72, 85, 65, 73, 0, 0}, {72, 85, 65, 78, 0, 0}, {72, 85, 65, 78, 71, 0}, {72, 85, 73, 0, 0, 0}, {72, 85, 78, 0, 0, 0}, {72, 85, 79, 0, 0, 0}, {74, 73, 0, 0, 0, 0}, {74, 73, 65, 0, 0, 0}, {74, 73, 65, 78, 0, 0}, {74, 73, 65, 78, 71, 0}, {74, 73, 65, 79, 0, 0}, {74, 73, 69, 0, 0, 0}, {74, 73, 78, 0, 0, 0}, {74, 73, 78, 71, 0, 0}, {74, 73, 79, 78, 71, 0}, {74, 73, 85, 0, 0, 0}, {74, 85, 0, 0, 0, 0}, {74, 85, 65, 78, 0, 0}, {74, 85, 69, 0, 0, 0}, {74, 85, 78, 0, 0, 0}, {75, 65, 0, 0, 0, 0}, {75, 65, 73, 0, 0, 0}, {75, 65, 78, 0, 0, 0}, {75, 65, 78, 71, 0, 0}, {75, 65, 79, 0, 0, 0}, {75, 69, 0, 0, 0, 0}, {75, 69, 78, 0, 0, 0}, {75, 69, 78, 71, 0, 0}, {75, 79, 78, 71, 0, 0}, {75, 79, 85, 0, 0, 0}, {75, 85, 0, 0, 0, 0}, {75, 85, 65, 0, 0, 0}, {75, 85, 65, 73, 0, 0}, {75, 85, 65, 78, 0, 0}, {75, 85, 65, 78, 71, 0}, {75, 85, 73, 0, 0, 0}, {75, 85, 78, 0, 0, 0}, {75, 85, 79, 0, 0, 0}, {76, 65, 0, 0, 0, 0}, {76, 65, 73, 0, 0, 0}, {76, 65, 78, 0, 0, 0}, {76, 65, 78, 71, 0, 0}, {76, 65, 79, 0, 0, 0}, {76, 69, 0, 0, 0, 0}, {76, 69, 73, 0, 0, 0}, {76, 69, 78, 71, 0, 0}, {76, 73, 0, 0, 0, 0}, {76, 73, 65, 0, 0, 0}, {76, 73, 65, 78, 0, 0}, {76, 73, 65, 78, 71, 0}, {76, 73, 65, 79, 0, 0}, {76, 73, 69, 0, 0, 0}, {76, 73, 78, 0, 0, 0}, {76, 73, 78, 71, 0, 0}, {76, 73, 85, 0, 0, 0}, {76, 79, 0, 0, 0, 0}, {76, 79, 78, 71, 0, 0}, {76, 79, 85, 0, 0, 0}, {76, 85, 0, 0, 0, 0}, {76, 85, 65, 78, 0, 0}, {76, 85, 69, 0, 0, 0}, {76, 85, 78, 0, 0, 0}, {76, 85, 79, 0, 0, 0}, {77, 0, 0, 0, 0, 0}, {77, 65, 0, 0, 0, 0}, {77, 65, 73, 0, 0, 0}, {77, 65, 78, 0, 0, 0}, {77, 65, 78, 71, 0, 0}, {77, 65, 79, 0, 0, 0}, {77, 69, 0, 0, 0, 0}, {77, 69, 73, 0, 0, 0}, {77, 69, 78, 0, 0, 0}, {77, 69, 78, 71, 0, 0}, {77, 73, 0, 0, 0, 0}, {77, 73, 65, 78, 0, 0}, {77, 73, 65, 79, 0, 0}, {77, 73, 69, 0, 0, 0}, {77, 73, 78, 0, 0, 0}, {77, 73, 78, 71, 0, 0}, {77, 73, 85, 0, 0, 0}, {77, 79, 0, 0, 0, 0}, {77, 79, 85, 0, 0, 0}, {77, 85, 0, 0, 0, 0}, {78, 0, 0, 0, 0, 0}, {78, 65, 0, 0, 0, 0}, {78, 65, 73, 0, 0, 0}, {78, 65, 78, 0, 0, 0}, {78, 65, 78, 71, 0, 0}, {78, 65, 79, 0, 0, 0}, {78, 69, 0, 0, 0, 0}, {78, 69, 73, 0, 0, 0}, {78, 69, 78, 0, 0, 0}, {78, 69, 78, 71, 0, 0}, {78, 73, 0, 0, 0, 0}, {78, 73, 65, 78, 0, 0}, {78, 73, 65, 78, 71, 0}, {78, 73, 65, 79, 0, 0}, {78, 73, 69, 0, 0, 0}, {78, 73, 78, 0, 0, 0}, {78, 73, 78, 71, 0, 0}, {78, 73, 85, 0, 0, 0}, {78, 79, 78, 71, 0, 0}, {78, 79, 85, 0, 0, 0}, {78, 85, 0, 0, 0, 0}, {78, 85, 65, 78, 0, 0}, {78, 85, 69, 0, 0, 0}, {78, 85, 78, 0, 0, 0}, {78, 85, 79, 0, 0, 0}, {79, 0, 0, 0, 0, 0}, {79, 85, 0, 0, 0, 0}, {80, 65, 0, 0, 0, 0}, {80, 65, 73, 0, 0, 0}, {80, 65, 78, 0, 0, 0}, {80, 65, 78, 71, 0, 0}, {80, 65, 79, 0, 0, 0}, {80, 69, 73, 0, 0, 0}, {80, 69, 78, 0, 0, 0}, {80, 69, 78, 71, 0, 0}, {80, 73, 0, 0, 0, 0}, {80, 73, 65, 78, 0, 0}, {80, 73, 65, 79, 0, 0}, {80, 73, 69, 0, 0, 0}, {80, 73, 78, 0, 0, 0}, {80, 73, 78, 71, 0, 0}, {80, 79, 0, 0, 0, 0}, {80, 79, 85, 0, 0, 0}, {80, 85, 0, 0, 0, 0}, {81, 73, 0, 0, 0, 0}, {81, 73, 65, 0, 0, 0}, {81, 73, 65, 78, 0, 0}, {81, 73, 65, 78, 71, 0}, {81, 73, 65, 79, 0, 0}, {81, 73, 69, 0, 0, 0}, {81, 73, 78, 0, 0, 0}, {81, 73, 78, 71, 0, 0}, {81, 73, 79, 78, 71, 0}, {81, 73, 85, 0, 0, 0}, {81, 85, 0, 0, 0, 0}, {81, 85, 65, 78, 0, 0}, {81, 85, 69, 0, 0, 0}, {81, 85, 78, 0, 0, 0}, {82, 65, 78, 0, 0, 0}, {82, 65, 78, 71, 0, 0}, {82, 65, 79, 0, 0, 0}, {82, 69, 0, 0, 0, 0}, {82, 69, 78, 0, 0, 0}, {82, 69, 78, 71, 0, 0}, {82, 73, 0, 0, 0, 0}, {82, 79, 78, 71, 0, 0}, {82, 79, 85, 0, 0, 0}, {82, 85, 0, 0, 0, 0}, {82, 85, 65, 0, 0, 0}, {82, 85, 65, 78, 0, 0}, {82, 85, 73, 0, 0, 0}, {82, 85, 78, 0, 0, 0}, {82, 85, 79, 0, 0, 0}, {83, 65, 0, 0, 0, 0}, {83, 65, 73, 0, 0, 0}, {83, 65, 78, 0, 0, 0}, {83, 65, 78, 71, 0, 0}, {83, 65, 79, 0, 0, 0}, {83, 69, 0, 0, 0, 0}, {83, 69, 78, 0, 0, 0}, {83, 69, 78, 71, 0, 0}, {83, 72, 65, 0, 0, 0}, {83, 72, 65, 73, 0, 0}, {83, 72, 65, 78, 0, 0}, {83, 72, 65, 78, 71, 0}, {83, 72, 65, 79, 0, 0}, {83, 72, 69, 0, 0, 0}, {83, 72, 69, 78, 0, 0}, {88, 73, 78, 0, 0, 0}, {83, 72, 69, 78, 0, 0}, {83, 72, 69, 78, 71, 0}, {83, 72, 73, 0, 0, 0}, {83, 72, 79, 85, 0, 0}, {83, 72, 85, 0, 0, 0}, {83, 72, 85, 65, 0, 0}, {83, 72, 85, 65, 73, 0}, {83, 72, 85, 65, 78, 0}, {83, 72, 85, 65, 78, 71}, {83, 72, 85, 73, 0, 0}, {83, 72, 85, 78, 0, 0}, {83, 72, 85, 79, 0, 0}, {83, 73, 0, 0, 0, 0}, {83, 79, 78, 71, 0, 0}, {83, 79, 85, 0, 0, 0}, {83, 85, 0, 0, 0, 0}, {83, 85, 65, 78, 0, 0}, {83, 85, 73, 0, 0, 0}, {83, 85, 78, 0, 0, 0}, {83, 85, 79, 0, 0, 0}, {84, 65, 0, 0, 0, 0}, {84, 65, 73, 0, 0, 0}, {84, 65, 78, 0, 0, 0}, {84, 65, 78, 71, 0, 0}, {84, 65, 79, 0, 0, 0}, {84, 69, 0, 0, 0, 0}, {84, 69, 78, 71, 0, 0}, {84, 73, 0, 0, 0, 0}, {84, 73, 65, 78, 0, 0}, {84, 73, 65, 79, 0, 0}, {84, 73, 69, 0, 0, 0}, {84, 73, 78, 71, 0, 0}, {84, 79, 78, 71, 0, 0}, {84, 79, 85, 0, 0, 0}, {84, 85, 0, 0, 0, 0}, {84, 85, 65, 78, 0, 0}, {84, 85, 73, 0, 0, 0}, {84, 85, 78, 0, 0, 0}, {84, 85, 79, 0, 0, 0}, {87, 65, 0, 0, 0, 0}, {87, 65, 73, 0, 0, 0}, {87, 65, 78, 0, 0, 0}, {87, 65, 78, 71, 0, 0}, {87, 69, 73, 0, 0, 0}, {87, 69, 78, 0, 0, 0}, {87, 69, 78, 71, 0, 0}, {87, 79, 0, 0, 0, 0}, {87, 85, 0, 0, 0, 0}, {88, 73, 0, 0, 0, 0}, {88, 73, 65, 0, 0, 0}, {88, 73, 65, 78, 0, 0}, {88, 73, 65, 78, 71, 0}, {88, 73, 65, 79, 0, 0}, {88, 73, 69, 0, 0, 0}, {88, 73, 78, 0, 0, 0}, {88, 73, 78, 71, 0, 0}, {88, 73, 79, 78, 71, 0}, {88, 73, 85, 0, 0, 0}, {88, 85, 0, 0, 0, 0}, {88, 85, 65, 78, 0, 0}, {88, 85, 69, 0, 0, 0}, {88, 85, 78, 0, 0, 0}, {89, 65, 0, 0, 0, 0}, {89, 65, 78, 0, 0, 0}, {89, 65, 78, 71, 0, 0}, {89, 65, 79, 0, 0, 0}, {89, 69, 0, 0, 0, 0}, {89, 73, 0, 0, 0, 0}, {89, 73, 78, 0, 0, 0}, {89, 73, 78, 71, 0, 0}, {89, 79, 0, 0, 0, 0}, {89, 79, 78, 71, 0, 0}, {89, 79, 85, 0, 0, 0}, {89, 85, 0, 0, 0, 0}, {89, 85, 65, 78, 0, 0}, {89, 85, 69, 0, 0, 0}, {89, 85, 78, 0, 0, 0}, {74, 85, 78, 0, 0, 0}, {89, 85, 78, 0, 0, 0}, {90, 65, 0, 0, 0, 0}, {90, 65, 73, 0, 0, 0}, {90, 65, 78, 0, 0, 0}, {90, 65, 78, 71, 0, 0}, {90, 65, 79, 0, 0, 0}, {90, 69, 0, 0, 0, 0}, {90, 69, 73, 0, 0, 0}, {90, 69, 78, 0, 0, 0}, {90, 69, 78, 71, 0, 0}, {90, 72, 65, 0, 0, 0}, {90, 72, 65, 73, 0, 0}, {90, 72, 65, 78, 0, 0}, {90, 72, 65, 78, 71, 0}, {67, 72, 65, 78, 71, 0}, {90, 72, 65, 78, 71, 0}, {90, 72, 65, 79, 0, 0}, {90, 72, 69, 0, 0, 0}, {90, 72, 69, 78, 0, 0}, {90, 72, 69, 78, 71, 0}, {90, 72, 73, 0, 0, 0}, {83, 72, 73, 0, 0, 0}, {90, 72, 73, 0, 0, 0}, {90, 72, 79, 78, 71, 0}, {90, 72, 79, 85, 0, 0}, {90, 72, 85, 0, 0, 0}, {90, 72, 85, 65, 0, 0}, {90, 72, 85, 65, 73, 0}, {90, 72, 85, 65, 78, 0}, {90, 72, 85, 65, 78, 71}, {90, 72, 85, 73, 0, 0}, {90, 72, 85, 78, 0, 0}, {90, 72, 85, 79, 0, 0}, {90, 73, 0, 0, 0, 0}, {90, 79, 78, 71, 0, 0}, {90, 79, 85, 0, 0, 0}, {90, 85, 0, 0, 0, 0}, {90, 85, 65, 78, 0, 0}, {90, 85, 73, 0, 0, 0}, {90, 85, 78, 0, 0, 0}, {90, 85, 79, 0, 0, 0}, {0, 0, 0, 0, 0, 0}, {83, 72, 65, 78, 0, 0}, {0, 0, 0, 0, 0, 0}};
    private static final Collator COLLATOR;
    private static HanziToPinyin sInstance;
    private final boolean mHasChinaCollator;

    private HanziToPinyin(boolean var1) {
        this.mHasChinaCollator = var1;
    }

    public static HanziToPinyin getInstance() {
        synchronized (HanziToPinyin.class) {
            if (sInstance != null) {
                return sInstance;
            } else {
                Locale[] var1 = Collator.getAvailableLocales();

                for (Locale var5 : var1) {
                    if (var5.equals(Locale.CHINA) || var5.getLanguage().equals("zh") && var5.getCountry().equals("HANS")) {
                        sInstance = new HanziToPinyin(true);
                        return sInstance;
                    }
                }

                sInstance = new HanziToPinyin(true);
                return sInstance;
            }
        }
    }

    private HanziToPinyin.Token getToken(char var1) {
        HanziToPinyin.Token var2 = new HanziToPinyin.Token();
        String var3 = Character.toString(var1);
        var2.source = var3;
        int var4 = -1;
        if (var1 < 256) {
            var2.type = 1;
            var2.target = var3;
            return var2;
        } else {
            int var5 = COLLATOR.compare(var3, "阿");
            if (var5 < 0) {
                var2.type = 3;
                var2.target = var3;
                return var2;
            } else {
                if (var5 == 0) {
                    var2.type = 2;
                    var4 = 0;
                } else {
                    var5 = COLLATOR.compare(var3, "\u9fff");
                    if (var5 > 0) {
                        var2.type = 3;
                        var2.target = var3;
                        return var2;
                    }

                    if (var5 == 0) {
                        var2.type = 2;
                        var4 = UNIHANS.length - 1;
                    }
                }

                var2.type = 2;
                int var7;
                if (var4 < 0) {
                    int var6 = 0;
                    var7 = UNIHANS.length - 1;

                    while (var6 <= var7) {
                        var4 = (var6 + var7) / 2;
                        String var8 = Character.toString(UNIHANS[var4]);
                        var5 = COLLATOR.compare(var3, var8);
                        if (var5 == 0) {
                            break;
                        }

                        if (var5 > 0) {
                            var6 = var4 + 1;
                        } else {
                            var7 = var4 - 1;
                        }
                    }
                }

                if (var5 < 0) {
                    --var4;
                }

                StringBuilder var9 = new StringBuilder();

                for (var7 = 0; var7 < PINYINS[var4].length && PINYINS[var4][var7] != 0; ++var7) {
                    var9.append((char) PINYINS[var4][var7]);
                }

                var2.target = var9.toString();
                if (TextUtils.isEmpty(var2.target)) {
                    var2.type = 3;
                    var2.target = var2.source;
                }

                return var2;
            }
        }
    }

    public ArrayList<Token> get(String var1) {
        ArrayList<Token> var2 = new ArrayList<>();
        if (this.mHasChinaCollator && !TextUtils.isEmpty(var1)) {
            int var3 = var1.length();
            StringBuilder var4 = new StringBuilder();
            int var5 = 1;

            for (int var6 = 0; var6 < var3; ++var6) {
                char var7 = var1.charAt(var6);
                if (var7 == 32) {
                    if (var4.length() > 0) {
                        this.addToken(var4, var2, var5);
                    }
                } else if (var7 < 256) {
                    if (var5 != 1 && var4.length() > 0) {
                        this.addToken(var4, var2, var5);
                    }

                    var5 = 1;
                    var4.append(var7);
                } else {
                    HanziToPinyin.Token var8 = this.getToken(var7);
                    if (var8.type == 2) {
                        if (var4.length() > 0) {
                            this.addToken(var4, var2, var5);
                        }

                        var2.add(var8);
                        var5 = 2;
                    } else {
                        if (var5 != var8.type && var4.length() > 0) {
                            this.addToken(var4, var2, var5);
                        }

                        var5 = var8.type;
                        var4.append(var7);
                    }
                }
            }

            if (var4.length() > 0) {
                this.addToken(var4, var2, var5);
            }

            return var2;
        } else {
            return var2;
        }
    }

    private void addToken(StringBuilder var1, ArrayList<HanziToPinyin.Token> var2, int var3) {
        String var4 = var1.toString();
        var2.add(new HanziToPinyin.Token(var3, var4, var4));
        var1.setLength(0);
    }

    static {
        COLLATOR = Collator.getInstance(Locale.CHINA);
    }

    public static class Token {
        public int type;
        String source;
        public String target;

        Token() {
        }

        Token(int var1, String var2, String var3) {
            this.type = var1;
            this.source = var2;
            this.target = var3;
        }
    }
}
