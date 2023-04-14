package com.yf.exam.modules.qu.ga;

import com.yf.exam.modules.exam.dto.ExamRepoDTO;
import com.yf.exam.modules.exam.dto.ext.ExamRepoExtDTO;
import com.yf.exam.modules.qu.entity.Qu;
import com.yf.exam.modules.qu.service.QuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Service
public class GAService {
    @Autowired
    private QuService quService;
    private List<List<Qu>> radioTK = new ArrayList<>();
    private List<List<Qu>> multiTK = new ArrayList<>();
    private List<List<Qu>> judgeTK = new ArrayList<>();

    private void update(List<ExamRepoExtDTO> list){
        List<Qu> temp;
        for (ExamRepoExtDTO item : list) {
            temp = quService.listByType(item.getRepoId(),1);
            radioTK.add(temp);
            temp = quService.listByType(item.getRepoId(),2);
            multiTK.add(temp);
            temp = quService.listByType(item.getRepoId(),3);
            judgeTK.add(temp);
        }
    }//获取题库中所有题目存于service中，一个List<Qu>为其中一个题库的所有单选题或多选题或判断题

    private CustomChromosome Init(List<ExamRepoExtDTO> list,double exceptScore) {
        Random random = new Random();
        int num = 0;
        List<Qu> genes = new ArrayList<>();
        //染色体内基因结构：题库1单选题|题库1多选题|题库1判断题|题库2单选题|题库2多选题|题库2判断题....

        for (ExamRepoExtDTO item : list) {
            List<Qu> temp = null;
            for (int k = 0; k < 3; k++) {
                int j = 0;
                switch (k) {
                    case 0:
                        j = item.getRadioCount();
                        temp = radioTK.get(list.indexOf(item));
                        break;
                    case 1:
                        j = item.getMultiCount();
                        temp = multiTK.get(list.indexOf(item));
                        break;
                    case 2:
                        j = item.getJudgeCount();
                        temp = judgeTK.get(list.indexOf(item));
                        break;
                }//选取构建染色体所需参数，题库count的k题型所需的题目数量以及其题库

                for (int i = 0; i < j; i++) {
                    do {
                        num = random.nextInt(temp.size());
                    } while (genes.contains(temp.get(num)));//随机选择未加入卷子的题目
                    genes.add(temp.get(num));
                }
            }//初始化基因内题库list.indexOf(item)的k类型题目
        }
        return new CustomChromosome(genes,list,exceptScore);
    }
    /**
        采用轮盘赌算法选择适应度较高的父代
     **/
    private List<CustomChromosome> Select(List<CustomChromosome>population,int count){
        double totalFitness = 0.0;
        Iterator<CustomChromosome> it = population.iterator();
        List<CustomChromosome> newPopulation = new ArrayList<>();
/*        while(it.hasNext())
        {
            CustomChromosome temp = it.next();
            temp.setFitness(exceptScore);
            totalFitness += temp.getFitness();
        }//计算个体适应度以及种群总适应度*/

        for(int i = 0;i < count;i++) {
            it = population.iterator();
            double slice = totalFitness * Math.random();
            double point = 0.0;

            while(it.hasNext())
            {
                CustomChromosome temp = it.next();
                point += temp.getFitness();
                //轮盘转动，当point>slice时轮盘选取至所选区块；优化：选择第一个大于平均适应度的个体
                if(point > slice && temp.getFitness() > (totalFitness / population.size()))
                {
                    newPopulation.add(temp);
                    break;
                }
            }
        }
        return newPopulation;
    }

    private List<CustomChromosome> Cross(List<CustomChromosome>population,int count,double exceptScore) {
        Random random = new Random();
        List<CustomChromosome> newPopulation = new ArrayList<>();
        double mp = 0.05;//变异概率

        for (int i = 0; i < count; i++) {
            int num1 = random.nextInt(population.size());
            int num2 = random.nextInt(population.size());//从父代随机选择两个进行交叉
            int exchange = random.nextInt(population.get(0).length());//随机选择交叉位置，此段代码为单点交叉
            int mutationCount = 0;

            CustomChromosome p1 = population.get(num1);
            CustomChromosome p2 = population.get(num2);
            List<Qu> genes1 = p1.getGenes();
            List<Qu> genes2 = p2.getGenes();

            for (int j = exchange; j < genes1.size(); j++) {
                Qu temp = genes1.get(j);
                if(genes1.contains(genes2.get(j)))
                {
                    Mutation(genes1,j,p1.getExamInfo());
                    mutationCount++;
                }//交叉产生重复题目，当场发生变异
                else
                    genes1.set(j, genes2.get(j));
                if(genes2.contains(genes1.get(j)))
                {
                    Mutation(genes2,j,p2.getExamInfo());
                    mutationCount++;
                }//交叉产生重复题目，当场发生变异
                else
                    genes2.set(j, temp);
            }

            if(Math.random() < mp && mutationCount == 0)
                genes1 = Mutation(genes1,0,p1.getExamInfo());//当genes1未因出现重复题目变异时发生变异
            if(Math.random() < mp && mutationCount == 0)
                genes2 = Mutation(genes2,0,p2.getExamInfo());//当genes2未因出现重复题目变异时发生变异

            CustomChromosome c1 = new CustomChromosome(genes1,p1.getExamInfo(),exceptScore);
            newPopulation.add(c1);
            CustomChromosome c2 = new CustomChromosome(genes2,p2.getExamInfo(),exceptScore);
            newPopulation.add(c2);
        }
        return newPopulation;
    }

    private List<Qu> Mutation(List<Qu>genes,int position,List<ExamRepoExtDTO> examInfo){
        System.out.println("发生变异！");
        Random random = new Random();
        if(position == 0)
            position = random.nextInt(genes.size());//随机选择变异位置
        List<Qu> tempTK = null;
        int repoIndex = 0;
        int quType = 1;

        int check = 0;
        for(ExamRepoDTO item:examInfo){
            check += item.getRadioCount();
            if(check > position)
            {
                repoIndex = examInfo.indexOf(item);
                quType = 1;
                break;
            }
            check += item.getMultiCount();
            if(check > position)
            {
                repoIndex = examInfo.indexOf(item);
                quType = 2;
                break;
            }
            check += item.getJudgeCount();
            if(check > position)
            {
                repoIndex = examInfo.indexOf(item);
                quType = 3;
                break;
            }
        }//采用类轮盘赌的方式确定变异位置所属题库以及题型

        switch (quType)
        {
            case 1: tempTK = radioTK.get(repoIndex);break;
            case 2: tempTK = multiTK.get(repoIndex);break;
            case 3: tempTK = judgeTK.get(repoIndex);break;
        }
        //获取变异点题型的题库，选择染色体内不重复的题目

        do{
            int num = random.nextInt(tempTK.size());
            if(!genes.contains(tempTK.get(num)))
            {
                System.out.println("position:" + position + " num:" + num);
                genes.set(position,tempTK.get(num));
                System.out.println(tempTK.get(num).toString());
                break;
            }
        }while(true);
        return genes;
    }
     public void Clear(){
        this.radioTK.clear();
        this.multiTK.clear();
        this.judgeTK.clear();
     }//问老师内会不会被清除

    public CustomChromosome geneticAlgorithm(double examDiff,List<ExamRepoExtDTO> list){
        List<CustomChromosome> population = new ArrayList<>();
        List<CustomChromosome> answerSet = new ArrayList<>();
        CustomChromosome result = null;
        update(list);

        for(int i = 0;i < 100;i++)
        {
            CustomChromosome chromosome = Init(list,examDiff);
            population.add(chromosome);
        }//初始化种群
        Iterator<CustomChromosome> it = population.iterator();
        int count = 1;
        System.out.println("初始种群如下：");
        while(it.hasNext())
        {
            CustomChromosome chromosome = it.next();
            System.out.println("第"+ count++ +"份试卷：");
            System.out.println(chromosome.toString());
        }

        for(int i = 0; i < 4;i++)//开始迭代
        {
            System.out.println("第" + i + "次迭代");
            population = Select(population, 50);
            System.out.println("选择一次，结果如下：");
            it = population.iterator();
            count = 1;
            while (it.hasNext()) {
                CustomChromosome chromosome = it.next();
                System.out.println("第" + count++ + "份试卷：");
                System.out.println(chromosome.toString());
                System.out.println("适应度为：" + chromosome.getFitness());
            }
            population = Cross(population, 50,examDiff);
            System.out.println("交叉一次，结果如下：");
            it = population.iterator();
            result = it.next();
            count = 1;
            while (it.hasNext()) {
                CustomChromosome chromosome = it.next();
                System.out.println("第" + count++ + "份试卷：");
                System.out.println(chromosome.toString());
                System.out.println("适应度为：" + chromosome.getFitness());
                if (chromosome.getFitness() > result.getFitness())
                    result = chromosome;
            }
            System.out.println("选择一次交叉一次后得到的结果：");
            System.out.println(result.toString());
            System.out.println("适应度为：" + result.getFitness());

            if(!answerSet.contains(result))
                answerSet.add(result);
        }

        it = answerSet.iterator();
        result = it.next();
        while (it.hasNext()) {
            CustomChromosome chromosome = it.next();
            if (chromosome.getFitness() > result.getFitness())
                result = chromosome;
        }
        System.out.println("遗传算法运行结束，最终结果如下：");
        System.out.println(result.toString());
        System.out.println("适应度为：" + result.getFitness());
        Clear();
        return result;
    }
}
