package tkaxv7s.xposed.sesame.model.base;

import tkaxv7s.xposed.sesame.data.Model;
import tkaxv7s.xposed.sesame.model.normal.answerAI.AnswerAI;
import tkaxv7s.xposed.sesame.model.normal.base.BaseModel;
import tkaxv7s.xposed.sesame.model.task.ancientTree.AncientTree;
import tkaxv7s.xposed.sesame.model.task.antCooperate.AntCooperate;
import tkaxv7s.xposed.sesame.model.task.antFarm.AntFarm;
import tkaxv7s.xposed.sesame.model.task.antForest.AntForestV2;
import tkaxv7s.xposed.sesame.model.task.antMember.AntMember;
import tkaxv7s.xposed.sesame.model.task.antOcean.AntOcean;
import tkaxv7s.xposed.sesame.model.task.antOrchard.AntOrchard;
import tkaxv7s.xposed.sesame.model.task.antSports.AntSports;
import tkaxv7s.xposed.sesame.model.task.antStall.AntStall;
import tkaxv7s.xposed.sesame.model.task.greenFinance.GreenFinance;
import tkaxv7s.xposed.sesame.model.task.reserve.Reserve;
import tkaxv7s.xposed.sesame.model.task.antDodo.AntDodo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModelOrder {

    private static final Class<Model>[] array = new Class[]{
            BaseModel.class//基础配置模块
            , AntForestV2.class//蚂蚁森林V2
            , AntFarm.class//庄园
            //, AntStall.class//新村
            , AntOrchard.class//农场
            //, Reserve.class//保护地
            , AntDodo.class//神奇物种
            , AntOcean.class//海洋
            //, AntCooperate.class//合种
            //, AncientTree.class//古树
            , AntSports.class//运动
            , AntMember.class//会员
            //, GreenFinance.class//绿色经营
            , AnswerAI.class//AI答题
    };

    private static final List<Class<Model>> readOnlyClazzList = Collections.unmodifiableList(Arrays.asList(array));

    public static List<Class<Model>> getClazzList() {
        return readOnlyClazzList;
    }

}