package stage2.task2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args){
        try( BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            while(true){
                String inputString = br.readLine();
                if(inputString.isEmpty()){
                    return;
                }
                inputString = inputString.replaceAll(",", ".");
                try {
                    System.out.println(Calculation.calculate(inputString).stripTrailingZeros().toPlainString());
                } catch (CalculationException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static class Calculation{

        private static final String valuesRegex = "(?<=\\().*(?=\\))|(-{0,1}\\d\\.{0,1}\\d*)++";
        private static final String operationsRegex = "(?<![+\\-*\\/^])[+\\-*\\/^]";
        private static final Pattern valuesPattern = Pattern.compile(valuesRegex);
        private static final Pattern operationsPattern = Pattern.compile(operationsRegex);

        private static final String nonDigitsRegex = "[a-zA-Z]";
        private static final Pattern nonDigitsPattern = Pattern.compile(nonDigitsRegex);

        static BigDecimal calculate(String input) throws CalculationException {
            try{
                return new BigDecimal(input);
            } catch(NumberFormatException ignored){
            }

            if(nonDigitsPattern.matcher(input).find()){
                throw new CalculationException("Error: Invalid equation.");
            }

            List<String> rawValues = new ArrayList<>();
            List<String> rawOperations = new ArrayList<>();

            tokenizeEquation(input, rawValues, rawOperations);

            List<BigDecimal> values = new LinkedList<>();
            for(String item: rawValues){
                values.add(calculate(item));
            }
            List<Operation> operations = new LinkedList<>();
            for (String rawOperation : rawOperations) {
                operations.add(Operation.getOperaton(rawOperation));
            }

            if(operations.size() == values.size() - 1){
                while(operations.size() > 0){
                    int curOperationIndex = Operation.indexOfMostPrioritizedOperation(operations);
                    BigDecimal newValue = calculate(
                            values.get(curOperationIndex),
                            values.get(curOperationIndex + 1),
                            operations.get(curOperationIndex));
                    values.set(curOperationIndex, newValue);
                    values.remove(curOperationIndex + 1);
                    operations.remove(curOperationIndex);
                }
                return values.get(0);
            }

            throw new CalculationException("Error: Invalid equation.");
        }

        private static BigDecimal calculate(BigDecimal value1, BigDecimal value2, Operation operation) throws CalculationException {
            try{
                switch(operation){
                    case ADDITION: return value1.add(value2);
                    case SUBTRACTION: return value1.subtract(value2);
                    case MULTIPLICATION: return value1.multiply(value2);
                    case DIVISION: return value1.divide(value2, 20, RoundingMode.HALF_UP);
                    case EXPONENTIATION: return pow(value1, value2);
                    default: return null;
                }
            } catch (ArithmeticException e){
                throw new CalculationException("Error: Division by zero.");
            }
        }

        private static BigDecimal pow(BigDecimal value1, BigDecimal value2){
            // X^(A+B)=X^A*X^B
            boolean value2IsNegative = false;
            if(value2.compareTo(BigDecimal.ZERO) < 0){
                value2IsNegative = true;
                value2 = value2.abs();
            }
            BigDecimal B = value2.remainder(BigDecimal.ONE);
            BigDecimal A = value2.subtract(B);
            BigDecimal XA = value1.pow(A.intValueExact());
            BigDecimal XB = new BigDecimal(Math.pow(value1.doubleValue(), B.doubleValue()));
            BigDecimal result;
            if(value2IsNegative){
                result = new BigDecimal(1).divide(XA.multiply(XB), 20, RoundingMode.HALF_UP);
            } else {
                result = XA.multiply(XB);
            }
            return result;
        }

        private static void tokenizeEquation(String equation,
                                             List<String> values,
                                             List<String> operations){
            Matcher matchedValues = valuesPattern.matcher(equation);
            while(matchedValues.find()){
                values.add(matchedValues.group(0));
            }
            String remainingEquation = equation.replaceAll("(?<=\\().*(?=\\))", "0");
            remainingEquation = remainingEquation.replaceAll("\\s+", "");
            Matcher matchedOperations = operationsPattern.matcher(remainingEquation);
            while(matchedOperations.find()){
                operations.add(matchedOperations.group(0));
            }
        }
    }

    enum Operation{
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION,
        EXPONENTIATION;

        public static int indexOfMostPrioritizedOperation(List<Operation> operations){
            int index = 0;
            int curMostPrioritizedOperationValue = 0;
            for (int i = 0; i < operations.size(); i++){
                if(operations.get(i).ordinal() >= curMostPrioritizedOperationValue){
                    index = i;
                    curMostPrioritizedOperationValue = operations.get(i).ordinal();
                }
            }
            return index;
        }

        public static Operation getOperaton(String operation){
            switch (operation){
                case "+": return ADDITION;
                case "-": return SUBTRACTION;
                case "*": return MULTIPLICATION;
                case "/": return DIVISION;
                case "^": return EXPONENTIATION;
                default: throw new EnumConstantNotPresentException(Operation.class, "Error: Invalid operation.");
            }
        }
    }
}

class CalculationException extends Exception{

    public CalculationException(String message){
        super(message);
    }
}
