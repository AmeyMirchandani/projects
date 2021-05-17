package project;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class MainNew {
    private static Scanner scan = new Scanner(System.in);
    public static void main(String[] args) {
        int[][] chart = {
            {80, 20, 0, 0, 0},
            {70, 15, 15, 0, 0},
            {60, 15, 15, 10, 0},
            {50, 20, 20, 10, 0},
            {40, 20, 20, 20, 0},
            {35, 25, 5, 30, 5},
            {20, 25, 25, 25, 5},
            {10, 20, 40, 20, 10},
            {5, 15, 40, 25, 15},
            {0, 5, 25, 30, 40}
        };

        int[] portfolio = recommendPortfolio(chart);
        String formattedArray = formatPortfolio(portfolio);

        System.out.println("Recommended Portfolio: " + formattedArray);
        
        recommendChanges(portfolio);

        scan.close();
    }

    private static void recommendChanges(int[] percents){
        BigDecimal[] values = getUserAllocation();
        BigDecimal[] targetValues = getProperAllocation(percents, values);
        BigDecimal[] offBy = getOffBy(values, targetValues);
        boolean done = false;
        while(!done){
            fixDuplicates(values, offBy);
            done = FixNonDuplicate(values, offBy);
        }
    }

    private static void printTransfer(BigDecimal amount, int from, int to){
        String fromString;
        String toString;

        if(from == 0)
            fromString = "Bonds";
        else if(from == 1)
            fromString = "Large Cap";
        else if(from == 2)
            fromString = "Mid Cap";
        else if(from == 3)
            fromString = "Foreign";
        else
            fromString = "Small Cap";
        
        if(to == 0)
            toString = "Bonds";
        else if(to == 1)
            toString = "Large Cap";
        else if(to == 2)
            toString = "Mid Cap";
        else if(to == 3)
            toString = "Foreign";
        else
            toString = "Small Cap";

        System.out.println("Transfer $" + amount.setScale(2, RoundingMode.HALF_UP) + " from " + fromString + " to " + toString + ".");
    }

    private static boolean FixNonDuplicate(BigDecimal[] values, BigDecimal[] offBy){
        int maxIndex = maxIndexAboveZero(offBy);
        int minIndex = MinIndexBelowZero(offBy);

        if(maxIndex == -1 || minIndex == -1)
            return true;
        else{
            if(offBy[maxIndex].compareTo(offBy[minIndex].multiply(BigDecimal.valueOf(-1.0))) > 0){
                values[minIndex] = values[minIndex].add(offBy[minIndex].multiply(BigDecimal.valueOf(-1.0)));
                values[maxIndex] = values[maxIndex].add(offBy[minIndex]);
                printTransfer(offBy[minIndex].multiply(BigDecimal.valueOf(-1.0)), maxIndex, minIndex);
                offBy[maxIndex] = offBy[maxIndex].add(offBy[minIndex]);
                offBy[minIndex] = BigDecimal.valueOf(0.0);
            }
            else{
                values[minIndex] = values[minIndex].add(offBy[maxIndex]);
                values[maxIndex] = values[maxIndex].subtract(offBy[maxIndex]);
                printTransfer(offBy[maxIndex], maxIndex, minIndex);
                offBy[minIndex] = offBy[minIndex].add(offBy[maxIndex]);
                offBy[maxIndex] = BigDecimal.valueOf(0.0);
            }
            return false;
        }
    }

    private static int maxIndexAboveZero(BigDecimal[] array){
        int index = -1;
        BigDecimal max = BigDecimal.valueOf(0.0);
        for(int i = 0; i < array.length; i++){
            if(array[i].compareTo(max) > 0){
                index = i;
                max = array[i];
            }
        }
        return index;
    }

    private static int MinIndexBelowZero(BigDecimal[] array){
        int index = -1;
        BigDecimal min = BigDecimal.valueOf(0.0);
        for(int i = 0; i < array.length; i++){
            if(array[i].compareTo(min) < 0){
                index = i;
                min = array[i];
            }
        }
        return index;
    }

    private static void fixDuplicates(BigDecimal[] values, BigDecimal[] offBy){
        while(true){
            int[] duplicateIndexes = getDuplicates(offBy);
            if(duplicateIndexes[0] == -1 || duplicateIndexes[1] == -1)
                return;
            else{
                if(offBy[duplicateIndexes[0]].compareTo(offBy[duplicateIndexes[1]]) > 0){
                    values[duplicateIndexes[0]] = values[duplicateIndexes[0]].subtract(offBy[duplicateIndexes[0]]);
                    values[duplicateIndexes[1]] = values[duplicateIndexes[1]].add(offBy[duplicateIndexes[0]]);
                    printTransfer(offBy[duplicateIndexes[0]], duplicateIndexes[0], duplicateIndexes[1]);
                }
                else{
                    values[duplicateIndexes[1]] = values[duplicateIndexes[1]].subtract(offBy[duplicateIndexes[1]]);
                    values[duplicateIndexes[0]] = values[duplicateIndexes[0]].add(offBy[duplicateIndexes[1]]);
                    printTransfer(offBy[duplicateIndexes[1]], duplicateIndexes[1], duplicateIndexes[0]);
                }
                offBy[duplicateIndexes[0]] = BigDecimal.valueOf(0.0);
                offBy[duplicateIndexes[1]] = BigDecimal.valueOf(0.0);
            }
        }
    }

    private static int[] getDuplicates(BigDecimal[] array){//gets elements that are off by the same number with a different sign
        int[] duplicateIndexes = {-1, -1};

        for(int i = 0; i < array.length-1; i++){
            for(int j = i+1; j < array.length; j++){
                if((!array[i].equals(BigDecimal.valueOf(0.0))) && (!array[j].equals(BigDecimal.valueOf(0.0)))){//ignore already fixed elements
                    if(array[i].multiply(BigDecimal.valueOf(-1.0)).equals(array[j])){
                        duplicateIndexes[0] = i;
                        duplicateIndexes[1] = j;
                        return duplicateIndexes;
                    }
                }
            }
        }
        return duplicateIndexes;
    }

    private static BigDecimal[] getUserAllocation(){
        BigDecimal[] userAllocations = new BigDecimal[5];

        System.out.println("Enter your current allocation in each category:\n1) = Bonds %, 2) = Large Cap %, 3) = Mid Cap %, 4) = Foreign %, 5) = Small Cap %");
        
        for(int i = 0; i < userAllocations.length; i++){
            try {
                System.out.print(i+1 + ") $");
                double input = scan.nextDouble();
                userAllocations[i] = BigDecimal.valueOf(dollarFormat(input));
            } catch (Exception e) {
                System.out.println("Invalid input, enter a decimal in the form xx.xx");
                i--;
                scan.next();
            }
        }
        return userAllocations;
    }

    private static BigDecimal[] getProperAllocation(int[] percents, BigDecimal[] values){
        BigDecimal[] properAllocations = new BigDecimal[values.length];
        BigDecimal valuesSum = sumArray(values);

        for(int i = 0; i < values.length; i++){
            properAllocations[i] = (valuesSum.multiply(BigDecimal.valueOf(percents[i]*1.0).divide(BigDecimal.valueOf(100.0))));
        }
        return properAllocations;
    }

    private static BigDecimal[] getOffBy(BigDecimal[] startValues, BigDecimal[] targetValues){
        BigDecimal[] offBy = new BigDecimal[startValues.length];

        for(int i = 0; i < startValues.length; i++){
            offBy[i] = startValues[i].subtract(targetValues[i]);
        }
        return offBy;
    }

    private static BigDecimal sumArray(BigDecimal[] array){
        BigDecimal sum = BigDecimal.valueOf(0.0);
        for(BigDecimal d : array){
            sum = sum.add(d);
        }
        return sum;
    }

    private static double dollarFormat(double amount){
        return ((int)(amount*100))/100.0;
    }
//Part 1 below
    private static int[] recommendPortfolio(int[][] array) {
        boolean done = false;
        int[] portfolio = null;
        while(done != true)
        {
            try {
                System.out.print("Enter your risk preference (1-10): ");
                int selection = scan.nextInt()-1;

                if(!(selection <= 9 && selection >= 0))
                {
                    System.out.println("Invalid input, enter an integer between 1 and 10");
                    continue;
                }

                portfolio = array[selection];
                done = true;
            } catch (Exception e) {
                System.out.println("Invalid input, enter an integer");
                scan.next();
            }
        }
        return portfolio;
    }

    private static String formatPortfolio(int[] array)
    {
        String str = "[";
        str += ("Bonds %: " + array[0] + ", ");
        str += ("Large Cap %: " + array[1] + ", ");
        str += ("Mid Cap %: " + array[2] + ", ");
        str += ("Foreign %: " + array[3] + ", ");
        str += ("Small Cap %: " + array[4]);
        str += "]";
        return str;
    }
}
