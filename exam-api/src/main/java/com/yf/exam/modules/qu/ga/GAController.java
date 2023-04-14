//package com.yf.exam.modules.qu.ga;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//@RestController
//public class GAController {
//    @Autowired
//    private GAService service;
//
//    @RequestMapping(value = "/test", method = {RequestMethod.GET})
//    public void test(){
//        List<CustomChromosome> population = new ArrayList<>();
//        List<CustomChromosome> answerSet = new ArrayList<>();
//        CustomChromosome result = null;
//
//        for(int i = 0;i < 100;i++)
//        {
//            CustomChromosome chromosome = service.Init(2,2,2);
//            population.add(chromosome);
//        }//初始化种群
//        Iterator<CustomChromosome> it = population.iterator();
//        int count = 1;
//        System.out.println("初始种群如下：");
//        while(it.hasNext())
//        {
//            CustomChromosome chromosome = it.next();
//            System.out.println("第"+ count++ +"份试卷：");
//            System.out.println(chromosome.toString());
//        }
//
//        for(int i = 0; i < 4;i++)//开始迭代
//        {
//            System.out.println("第" + i + "次迭代");
//            population = service.Select(population, 50);
//            System.out.println("选择一次，结果如下：");
//            it = population.iterator();
//            count = 1;
//            while (it.hasNext()) {
//                CustomChromosome chromosome = it.next();
//                System.out.println("第" + count++ + "份试卷：");
//                System.out.println(chromosome.toString());
//                System.out.println("适应度为：" + chromosome.getFitness());
//            }
//            population = service.Cross(population, 50);
//            System.out.println("交叉一次，结果如下：");
//            it = population.iterator();
//            result = it.next();
//            count = 1;
//            while (it.hasNext()) {
//                CustomChromosome chromosome = it.next();
//                System.out.println("第" + count++ + "份试卷：");
//                System.out.println(chromosome.toString());
//                System.out.println("适应度为：" + chromosome.getFitness());
//                if (chromosome.getFitness() > result.getFitness())
//                    result = chromosome;
//            }
//            System.out.println("选择一次交叉一次后得到的结果：");
//            System.out.println(result.toString());
//            System.out.println("适应度为：" + result.getFitness());
//
//            if(!answerSet.contains(result))
//                answerSet.add(result);
//        }
//
//        it = answerSet.iterator();
//        result = it.next();
//        while (it.hasNext()) {
//            CustomChromosome chromosome = it.next();
//            if (chromosome.getFitness() > result.getFitness())
//                result = chromosome;
//        }
//        System.out.println("遗传算法运行结束，最终结果如下：");
//        System.out.println(result.toString());
//        System.out.println("适应度为：" + result.getFitness());
//        service.Clear();
//    }
//}
