package stage2.task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Main {
    public static void main(String[] args){
        Pair<String, Boolean>[] inputArgs = new Pair[args.length];
        for(int i = 0 ; i < args.length ; i++){
            try{
                Pattern.compile(args[i]);
                inputArgs[i] = new Pair<>(args[i], true);
            } catch (PatternSyntaxException ignored){
                inputArgs[i] = new Pair<>(args[i], false);
            }
        }

        List<String> result = new LinkedList<>();

        try( BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            while(true){
                String inputString = br.readLine();
                if(inputString.isEmpty()){
                    break;
                }
                String[] inputWords = inputString.split(" ");
                _outerLoop:
                for(int i = 0 ; i < inputArgs.length; i++){
                    for(int j = 0 ; j < inputWords.length; j++){
                        // if valid regexp
                        if(inputArgs[i].value){
                            if(inputWords[j].matches(inputArgs[i].key)){
                                result.add(inputString);
                                break _outerLoop;
                            }
                        } else { // otherwise
                            if(inputWords[j].equals(inputArgs[i].key)){
                                result.add(inputString);
                                break _outerLoop;
                            }
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }

        result.forEach(System.out::println);
    }

    public static class Pair<K, V>{
        public K key;
        public V value;

        public Pair(K key, V value){
            this.key = key;
            this.value = value;
        }
    }
}
