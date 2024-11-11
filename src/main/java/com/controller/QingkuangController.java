
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 实习情况
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/qingkuang")
public class QingkuangController {
    private static final Logger logger = LoggerFactory.getLogger(QingkuangController.class);

    private static final String TABLE_NAME = "qingkuang";

    @Autowired
    private QingkuangService qingkuangService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private BanjiService banjiService;//班级
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private GonggaoService gonggaoService;//公告
    @Autowired
    private GongsiService gongsiService;//公司
    @Autowired
    private JianliService jianliService;//简历
    @Autowired
    private LaoshiService laoshiService;//老师
    @Autowired
    private PingfenService pingfenService;//实习评分
    @Autowired
    private ToudiService toudiService;//简历投递
    @Autowired
    private YonghuService yonghuService;//学生
    @Autowired
    private YuanxiService yuanxiService;//院系
    @Autowired
    private ZhaopinService zhaopinService;//职位招聘
    @Autowired
    private ZuoyeService zuoyeService;//实习作业
    @Autowired
    private ZuoyeTijiaoService zuoyeTijiaoService;//作业提交
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("学生".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("公司".equals(role))
            params.put("gongsiId",request.getSession().getAttribute("userId"));
        else if("老师".equals(role))
            params.put("laoshiId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = qingkuangService.queryPage(params);

        //字典表数据转换
        List<QingkuangView> list =(List<QingkuangView>)page.getList();
        for(QingkuangView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        QingkuangEntity qingkuang = qingkuangService.selectById(id);
        if(qingkuang !=null){
            //entity转view
            QingkuangView view = new QingkuangView();
            BeanUtils.copyProperties( qingkuang , view );//把实体数据重构到view中
            //级联表 公司
            //级联表
            GongsiEntity gongsi = gongsiService.selectById(qingkuang.getGongsiId());
            if(gongsi != null){
            BeanUtils.copyProperties( gongsi , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"
, "gongsiId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setGongsiId(gongsi.getId());
            }
            //级联表 学生
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(qingkuang.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"
, "gongsiId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody QingkuangEntity qingkuang, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,qingkuang:{}",this.getClass().getName(),qingkuang.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("学生".equals(role))
            qingkuang.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        else if("公司".equals(role))
            qingkuang.setGongsiId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        Wrapper<QingkuangEntity> queryWrapper = new EntityWrapper<QingkuangEntity>()
            .eq("gongsi_id", qingkuang.getGongsiId())
            .eq("yonghu_id", qingkuang.getYonghuId())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        QingkuangEntity qingkuangEntity = qingkuangService.selectOne(queryWrapper);
//        if(qingkuangEntity==null){
            qingkuang.setInsertTime(new Date());
            qingkuang.setCreateTime(new Date());
            qingkuangService.insert(qingkuang);
            return R.ok();
//        }else {
//            return R.error(511,"表中有相同数据");
//        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody QingkuangEntity qingkuang, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,qingkuang:{}",this.getClass().getName(),qingkuang.toString());
        QingkuangEntity oldQingkuangEntity = qingkuangService.selectById(qingkuang.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("学生".equals(role))
//            qingkuang.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
//        else if("公司".equals(role))
//            qingkuang.setGongsiId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        if("".equals(qingkuang.getQingkuangText()) || "null".equals(qingkuang.getQingkuangText())){
                qingkuang.setQingkuangText(null);
        }

            qingkuangService.updateById(qingkuang);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<QingkuangEntity> oldQingkuangList =qingkuangService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        qingkuangService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<QingkuangEntity> qingkuangList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            QingkuangEntity qingkuangEntity = new QingkuangEntity();
//                            qingkuangEntity.setGongsiId(Integer.valueOf(data.get(0)));   //公司 要改的
//                            qingkuangEntity.setYonghuId(Integer.valueOf(data.get(0)));   //学生 要改的
//                            qingkuangEntity.setQingkuangText(data.get(0));                    //实习情况 要改的
//                            qingkuangEntity.setInsertTime(date);//时间
//                            qingkuangEntity.setCreateTime(date);//时间
                            qingkuangList.add(qingkuangEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        qingkuangService.insertBatch(qingkuangList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




}

