package project;

import java.util.Scanner;

public class Main {
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
        double[] values = getUserAllocation();
        double[] targetValues = getProperAllocation(percents, values);
        for(double d : targetValues){
            System.out.println(d);
        }
        double[] offBy = getOffBy(values, targetValues);
        boolean done = false;
        while(!done){
            fixDuplicates(values, offBy);
            done = FixNonDuplicate(values, offBy);
            for(double d : values){
                System.out.println(d);
            }
            for(double d : offBy){
                System.out.println(d);
            }
            System.out.println("\n\n");
        }
    }

    private static void printTransfer(double amount, int from, int to){
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

        System.out.println("Transfer $" + amount + " from " + fromString + " to " + toString + ".");
    }

    private static boolean FixNonDuplicate(double[] values, double[] offBy){
        int maxIndex = maxIndexAboveZero(offBy);
        int minIndex = MinIndexBelowZero(offBy);

        if(maxIndex == -1 || minIndex == -1)
            return true;
        else{
            if(offBy[maxIndex] > -1*offBy[minIndex]){
                values[minIndex] += (-1*offBy[minIndex]);
                values[maxIndex] += offBy[minIndex];
                printTransfer(-1*offBy[minIndex], maxIndex, minIndex);
                offBy[maxIndex] += offBy[minIndex];
                offBy[minIndex] = 0;
            }
            else{
                values[minIndex] += (offBy[maxIndex]);
                values[maxIndex] -= offBy[maxIndex];
                printTransfer(offBy[maxIndex], maxIndex, minIndex);
                offBy[minIndex] += offBy[maxIndex];
                offBy[maxIndex] = 0;
            }
            return false;
        }
    }

    private static int maxIndexAboveZero(double[] array){
        int index = -1;
        double max = 0;
        for(int i = 0; i < array.length; i++){
            if(array[i] > max){
                index = i;
                max = array[i];
            }
        }
        return index;
    }

    private static int MinIndexBelowZero(double[] array){
        int index = -1;
        double min = 0;
        for(int i = 0; i < array.length; i++){
            if(array[i] < min){
                index = i;
                min = array[i];
            }
        }
        return index;
    }

    private static void fixDuplicates(double[] values, double[] offBy){
        while(true){
            int[] duplicateIndexes = getDuplicates(offBy);
            if(duplicateIndexes[0] == -1 || duplicateIndexes[1] == -1)
                return;
            else{
                if(offBy[duplicateIndexes[0]] > offBy[duplicateIndexes[1]]){
                    values[duplicateIndexes[0]] -= offBy[duplicateIndexes[0]];
                    values[duplicateIndexes[1]] += offBy[duplicateIndexes[0]];
                    printTransfer(offBy[duplicateIndexes[0]], duplicateIndexes[0], duplicateIndexes[1]);
                }
                else{
                    values[duplicateIndexes[1]] -= offBy[duplicateIndexes[1]];
                    values[duplicateIndexes[0]] += offBy[duplicateIndexes[1]];
                    printTransfer(offBy[duplicateIndexes[1]], duplicateIndexes[1], duplicateIndexes[0]);
                }
                offBy[duplicateIndexes[0]] = 0;
                offBy[duplicateIndexes[1]] = 0;
            }
        }
    }

    private static int[] getDuplicates(double[] array){//gets elements that are off by the same number with a different sign
        int[] duplicateIndexes = {-1, -1};

        for(int i = 0; i < array.length-1; i++){
            for(int j = i+1; j < array.length; j++){
                if(array[i] != 0 && array[j] != 0){//ignore already fixed elements
                    if(array[i]*-1 == array[j]){
                        duplicateIndexes[0] = i;
                        duplicateIndexes[1] = j;
                        return duplicateIndexes;
                    }
                }
            }
        }
        return duplicateIndexes;
    }

    private static double[] getUserAllocation(){
        double[] userAllocations = new double[5];

        System.out.println("Enter your current allocation in each category:\n1) = Bonds %, 2) = Large Cap %, 3) = Mid Cap %, 4) = Foreign %, 5) = Small Cap %");
        
        for(int i = 0; i < userAllocations.length; i++){
            try {
                System.out.print(i+1 + ") $");
                double input = scan.nextDouble();
                userAllocations[i] = dollarFormat(input);
            } catch (Exception e) {
                System.out.println("Invalid input, enter a decimal in the form xx.xx");
                i--;
                scan.next();
            }
        }
        return userAllocations;
    }

    private static double[] getProperAllocation(int[] percents, double[] values){
        double[] properAllocations = new double[values.length];
        double valuesSum = sumArray(values);

        for(int i = 0; i < values.length; i++){
            properAllocations[i] = (valuesSum * (percents[i]/100.0));
        }
        return properAllocations;
    }

    private static double[] getOffBy(double[] startValues, double[] targetValues){
        double[] offBy = new double[startValues.length];

        for(int i = 0; i < startValues.length; i++){
            offBy[i] = startValues[i] - targetValues[i];
        }
        return offBy;
    }

    private static double sumArray(double[] array){
        double sum = 0;
        for(double d : array){
            sum += d;
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
