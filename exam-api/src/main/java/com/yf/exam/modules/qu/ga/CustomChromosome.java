package com.yf.exam.modules.qu.ga;

import com.yf.exam.modules.exam.dto.ExamRepoDTO;
import com.yf.exam.modules.exam.dto.ext.ExamRepoExtDTO;
import com.yf.exam.modules.qu.entity.Qu;
import java.util.List;

public class CustomChromosome{

    private List<Qu> genes;

    private List<ExamRepoExtDTO> examInfo;

    private int radioNum;
    private int multiNum;
    private int judgeNum;

    private double fitness;
    public CustomChromosome(List<Qu> genes,List<ExamRepoExtDTO> examInfo,double exceptScore){
        this.genes = genes;
        this.examInfo = examInfo;
        this.radioNum = 0;
        this.multiNum = 0;
        this.judgeNum = 0;

        for(ExamRepoDTO item:examInfo)
        {
            radioNum += item.getRadioCount();
            multiNum += item.getMultiCount();
            judgeNum += item.getJudgeCount();
        }
        setFitness(exceptScore);
    }

    public int length(){
        return genes.size();
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double exceptScore) {
        double score = 0;//本试卷的期望平均分
        int repoIndex = 0;
        int position = examInfo.get(repoIndex).getRadioCount()
                + examInfo.get(repoIndex).getMultiCount()
                + examInfo.get(repoIndex).getJudgeCount() - 1;
        int oneScore = 0;
        for(int i = 0;i < length();i++)
        {
            if(i > position)
            {
                repoIndex++;
                position += examInfo.get(repoIndex).getRadioCount();
                position += examInfo.get(repoIndex).getMultiCount();
                position += examInfo.get(repoIndex).getJudgeCount();
            }

            switch (genes.get(i).getQuType())
            {
                case 1: oneScore = examInfo.get(repoIndex).getRadioScore();break;
                case 2: oneScore = examInfo.get(repoIndex).getMultiScore();break;
                case 3: oneScore = examInfo.get(repoIndex).getJudgeScore();break;
            }
            score += (1-genes.get(i).getLevel()) * oneScore;//level意义为本题错误率，计算期望分数为正确率*分值
        }
        System.out.println("试卷期望平均分" + score);
        this.fitness = 1 - (Math.abs(score - exceptScore) / exceptScore);//适应度即为期望平均分与实际平均分的偏移
    }

    public List<Qu> getGenes() {
        return genes;
    }

    public List<ExamRepoExtDTO> getExamInfo() {
        return examInfo;
    }


    @Override
    public String toString() {
        return "CustomChromosome{" +
                "genes=" + genes.toString() +
                ",fitness=" + fitness +
                '}';
    }
}
