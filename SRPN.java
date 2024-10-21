import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Program class for an SRPN calculator. Current it outputs "0" for every "=" sign.
 */

/*
* Please use Java version 18 or above to run this program, as some functions, e.g. divideExact is only available since 18
* */
public class SRPN {
    private Stack<Integer> stack;
    private int indexOfRandomList;
    private boolean commenting;

    /* An operand can optionally begin with the negative sign and have at least 1 digit */
    private static final Pattern OPERAND_PATTERN = Pattern.compile("^[-]?[\\d]+");

    /* Compound assignment operators begins with one of + - * / % ^ at least once and end with an assignment e.g. += or ++= */
    private static final Pattern COMPOUND_ASSIGNMENT_PATTERN = Pattern.compile("^[+\\-*/^%]+=");

    /* Infix are those begin with one of + - * / % ^ once and end with an operand e.g. +1 or +-1 */
    private static final Pattern INFIX_PATTERN = Pattern.compile("^[+\\-*%^][-]?[\\d]+");

    private static final ArrayList<Integer> RANDOM_LIST = new ArrayList<>(Arrays.asList(
            1804289383,846930886,1681692777,1714636915,1957747793,424238335, 719885386,
            1649760492,596516649,1189641421,1025202362,1350490027,783368690,1102520059,
            2044897763,1967513926,1365180540,1540383426,304089172,1303455736,35005211,
            521595368,1804289383));

    private static final String DIVIDE_ZERO_MESSAGE = "Divide by 0.";
    private static final String NEGATIVE_POWER_MESSAGE = "Negative power.";
    private static final String STACK_EMPTY_MESSAGE = "Stack empty.";
    private static final String STACK_UNDERFLOW_MESSAGE ="Stack underflow.";
    private static final String STACK_OVERFLOW_MESSAGE = "Stack overflow.";

    public SRPN() {
        this.stack = new Stack<>();
        this.indexOfRandomList =0;
        this.commenting = false;
    }

    /*
     * processCommand() uses pattern matching and recursion to break down the expression
     *
     * Case A Postfix Expression: "3 3 +"
     * (1) The first "3" hits the operandMatcher. "3" is passed to process() and the remaining " 3 +" is recursively passed to processCommand()
     * (2) The empty space is stripped, so the expression becomes "3 +".
     *     The second "3" hits the operandMatcher. "3" is passed to process() and the remaining " +" is recursively passed to processCommand()
     * (3) The empty space is stripped, so the expression becomes "+".
     *     "+" is neither an operand nor an infix expression nor a compound assignment operator, so it hits the else-clause. "+" is therefore passed to process() and the remaining "" is recursively passed to processCommand().
     * (4) The recursion finally ends since the expression is now an empty string
     *
     * Case B Infix Expression: "3+3"
     * (1) The first "3" hits the operandMatcher. "3" is passed to process() and the remaining "+3" is recursively passed to processCommand().
     * (2) The "+3" hits the infixMatcher. We revise it to "3+" so it becomes a postfix expression and is passed to processCommand()
     * (3) With the expression now becomes "3+", this becomes Case A step 2 - 4
     *
     * Case C Compound Assignment Operator: "3 3 +="
     * (1) The first "3" hits the operandMatcher. "3" is therefore to process() and the remaining " 3 +=" is recursively passed to processCommand()
     * (2) The empty is space is stripped, so the expression becomes "3 +=".
     *     The second "3" hits the operandMatcher. "3" is therefore to process() and the remaining " +=" is recursively passed to processCommand()
     * (3) The empty is space is stripped, so the expression becomes "+=".
     *     The "+=" hits the compound assignment operator. We first handle the assignment operator "=" by passing it to process() and the remaining "+" is recursively passed to processCommand()
     * (4) With the expression now becomes "+", the becomes Case A step 3 - 4
     * */
    public void processCommand(String s) {
        String stripped = s.strip();
        Matcher operandMatcher = OPERAND_PATTERN.matcher(stripped);
        Matcher compoundAssignmentPatternMatcher = COMPOUND_ASSIGNMENT_PATTERN.matcher(stripped);
        Matcher infixMatcher = INFIX_PATTERN.matcher(stripped);
        if (s.isBlank()) return;
        else if(operandMatcher.find()) {
            String operand = stripped.substring(0, operandMatcher.end());
            String remaining = stripped.substring(operandMatcher.end());
            process(operand);
            processCommand(remaining);
        }
        else if(infixMatcher.find()) {
            String operator = stripped.substring(0, 1);
            String operand = stripped.substring(1, infixMatcher.end());
            String remaining = stripped.substring(infixMatcher.end());
            String revised = operand + operator + remaining;
            processCommand(revised);
        }
        else if(compoundAssignmentPatternMatcher.find()) {
            String operators = stripped.substring(0, compoundAssignmentPatternMatcher.end()-1);
            String remaining = stripped.substring(compoundAssignmentPatternMatcher.end());
            String revised = remaining + operators;
            process("=");
            processCommand(revised);
        }
        else {
            String first = stripped.substring(0, 1);
            String remaining = stripped.substring(1);
            process(first);
            processCommand(remaining);
        }
    }

    /*
     * The method categorizes operator, operand and unrecognised string and pass it to be handled by the corresponding functions
     * For example, if the string is an operand i.e. "2", it wil be handled by pushNumber();.
     */
    public void process(String s) {
        if (s.isBlank()) return;
        else if (s.equals("#")) toggleCommenting();
        else if (commenting) return;
        else if (OPERAND_PATTERN.matcher(s).matches()) pushNumber(s);
        else if (s.equals("+")) add();
        else if (s.equals("-")) subtract();
        else if (s.equals("*")) multiply();
        else if (s.equals("/")) divide();
        else if (s.equals("%")) modulo();
        else if (s.equals("=")) assignment();
        else if (s.equals("^")) pow();
        else if (s.equals("d")) display();
        else if (s.equals("r")) random();
        else unrecognised(s);
    }

    /* The default value of commenting is false, when input is "#"(process("#")),the commenting status switches to true */
    public void toggleCommenting() {
        commenting = !commenting;
    }

    /*
     * This function pushes the number to stack. When the number is larger than MAX ,it pushes MAX to stack.
     * When the number is smaller than MIN ,it pushes MIN to stack.
     */
    public void pushNumber(String s) {
        try{
            int number = Integer.parseInt(String.valueOf(s));
            stack.push(number);
        } catch (NumberFormatException e){
            if (s.charAt(0) == '-') stack.push(Integer.MIN_VALUE);
            else stack.push(Integer.MAX_VALUE);
        }
    }

    /* This is called by all arithmetic operations to ensure we have at least 2 numbers in stack */
    public boolean safePop(){
        boolean isValid = stack.size() >= 2;
        if(!isValid) System.out.println(STACK_UNDERFLOW_MESSAGE);
        return isValid;
    }

    /*
     * This function performs addition
     * If the result is larger than MAX, e.g. 2147483647 1 + we push back MAX
     * If the result is smaller than MIN, e.g. -2147483648 -1 +, we push back MIN
     **/
    public void add () {
        if (safePop()) {
            int firstPop = stack.pop();
            int secondPop = stack.pop();
            try {
                int result = Math.addExact(secondPop, firstPop);
                stack.push(result);
            } catch (ArithmeticException e) {
                if (firstPop > 0) stack.push(Integer.MAX_VALUE);
                else stack.push(Integer.MIN_VALUE);
            }
        }
    }

    /*
    * This method performs subtraction
    * If the result is larger than MAX, e.g. 1 -2147483648 -, we push back MAX
    * If the result is smaller than MIN, e.g. -1 2147483648 -, we push back MIN
    * */
    public void subtract() {
        if (safePop()) {
            int firstPop = stack.pop();
            int secondPop = stack.pop();
            try {
                int result = Math.subtractExact(secondPop, firstPop);
                stack.push(result);
            } catch (ArithmeticException e) {
                if (secondPop < 0) stack.push(Integer.MIN_VALUE);
                else stack.push(Integer.MAX_VALUE);
            }
        }
    }

    /*
     * This method performs multiplication
     * If the results is larger than MAX, e.g. 2147483647 2147483647 *, -2147483648 -2147483648 *, we push back MAX
     * If the results is smaller than MIN, e.g. 2147483647 -2147483648 *, -2147483647 2147483647 *, we push back MIN
     */
    public void multiply() {
        if (safePop()) {
            int firstPop = stack.pop();
            int secondPop = stack.pop();
            try {
                int result = Math.multiplyExact(secondPop,firstPop);
                stack.push(result);
            } catch (ArithmeticException e) {
                if (Math.signum(firstPop) == Math.signum(secondPop)) stack.push(Integer.MAX_VALUE);
                else stack.push(Integer.MIN_VALUE);
            }
        }
    }

    /*
     * This method performs division
     * This handles division by zero, e.g. 10 0 /, it should print out the error message and pushes back the numbers
     * If the result is larger than MAX, e.g. -2147483648 -1 /, we push back MAX
     */
    public void divide () {
        if (safePop()) {
            int firstPop = stack.pop();
            int secondPop = stack.pop();
            try {
                if (firstPop == 0) {
                    System.out.println(DIVIDE_ZERO_MESSAGE);
                    stack.push(secondPop);
                    stack.push(firstPop);
                }
                else {
                    int result = Math.divideExact(secondPop, firstPop);
                    stack.push(result);
                }
            } catch (ArithmeticException e) {
                if (Math.signum(firstPop) == Math.signum(secondPop)) stack.push(Integer.MAX_VALUE);
                else stack.push(Integer.MIN_VALUE);
            }
        }
    }

    /*
    * This method performs modulo
    * This handles division by zero, e.g. 0 5 %, it should print out the error message and pushes back the numbers
    * This also mimics the original C++ program's core dump error when we do 5 0 %
    * */
    public void modulo () {
        if (safePop()) {
            int firstPop = stack.pop();
            int secondPop = stack.pop();
            if (secondPop == 0) {
                System.out.println(DIVIDE_ZERO_MESSAGE);
                stack.push(secondPop);
                stack.push(firstPop);
            } else {
                int result = secondPop % firstPop;
                stack.push(result);
            }
        }
    }

    /*
     * This method performs pow
     * This handles negative power which an error message will be printed out and the numbers will be pushed back
     * If the results is larger than MAX, e.g. -2000000000 2 ^, we push back MAX
     * If the results is larger than MAX, e.g. -2000000000 3 ^, we push back MIN
     */
    public void pow() {
        if (safePop()) {
            int firstPop = stack.pop();
            int secondPop = stack.pop();
            try{
                if (firstPop < 0) {
                    stack.push(secondPop);
                    stack.push(firstPop);
                    System.out.println(NEGATIVE_POWER_MESSAGE);
                } else {
                    int result = (int) Math.pow(secondPop, firstPop);
                    stack.push(result);
                }
            } catch (ArithmeticException e) {
                if (secondPop > 0 || firstPop % 2 == 0) stack.push(Integer.MAX_VALUE);
                else stack.push(Integer.MIN_VALUE);
            }
        }
    }

    /*
    * This method print the top element when the stack is not empty, otherwise it prints the error message
    * */
    public void assignment() {
        if (!stack.empty()) {
            int result = stack.peek();
            System.out.println(result);
        }
        else System.out.println(STACK_EMPTY_MESSAGE);
    }

    /*
    * This method loops through each element in the stack and print them out. If the stack is empty, it prints MIN
    * */
    public void display( ) {
        if (stack.isEmpty()) System.out.println(Integer.MIN_VALUE);
        else stack.forEach(System.out::println);
    }

    /*
     * This method push the number from the random list if the list is not yet exhausted. Otherwise, we print out the error message
     * We use indexOfRandomList to keep track of our current position in the random list
     */
    public void random(){
        if (indexOfRandomList >= RANDOM_LIST.size())System.out.println(STACK_OVERFLOW_MESSAGE);
        else stack.push(RANDOM_LIST.get(indexOfRandomList++));
    }

    /*
    * This method prints out the error message for any unrecognized input
    * */
    public void unrecognised(String s){
        System.out.printf("Unrecognised operator or operand \"%s\".%n", s);
    }
}





