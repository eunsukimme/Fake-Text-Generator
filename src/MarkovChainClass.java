import java.io.*;
import java.util.StringTokenizer;
import java.util.Random;
import java.util.Vector;

public class MarkovChainClass {
    private int NHASH = 4093;
    private int MULTIPLIER = 31;
    private int MAX = 200;
    Node[] htable = new Node[NHASH];
    String inputString;

    public static void main(String argc[]) throws Exception{
        InputStream in = System.in;
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);
        MarkovChainClass MKCC = new MarkovChainClass();

        System.out.print("읽을 파일 이름을 입력하시오(ex. harry.txt): ");
        MKCC.inputString = br.readLine();
        if(!MKCC.ReadFile(br))
            return;
        System.out.println("File Read.");
        //MKCC.PrintHashTable();  해쉬테이블 제대로 만들어졌는지 확인
        System.out.println("Markov Chain >>" + "\n");
        MKCC.PrintFakeText();


    }
    public boolean ReadFile(BufferedReader br) throws IOException{
        try {
            br = new BufferedReader(new FileReader(inputString));
        }catch(FileNotFoundException e) {
            System.out.println("No such file exist.txt");
            return false;
        }
        boolean EOF = false;
        String key1 = null, key2 = null, suffix = null, token = null;
        String lineBuffer = br.readLine();
        StringTokenizer tokenizer = new StringTokenizer(lineBuffer, " ");
        while(true){
            token = tokenizer.nextToken();
            key1 = key2;
            key2 = suffix;
            suffix = token;

            if(key1 == null){
                // key1이 null인데 토큰이 없다면
                if(!tokenizer.hasMoreTokens()){
                    // 줄 바꿔서 다음 토큰 가져옴
                    while(true) {
                        if ((lineBuffer = br.readLine()) == null)
                            EOF = true;
                        else if(lineBuffer.isEmpty())
                            continue;
                        break;
                    }
                    if(!EOF)
                        tokenizer = new StringTokenizer(lineBuffer, " ");
                    continue;
                }
                continue;
            }
            // 다음 토큰이 없다면(줄의 끝 이라면) 다음 줄 읽어놓음(다음 토큰은 다음 줄의 첫 단어)
            else if(!tokenizer.hasMoreTokens()){
                while(true) {
                    if ((lineBuffer = br.readLine()) == null)
                        EOF = true;
                    else if(lineBuffer.isEmpty())
                        continue;
                    break;
                }
                if(!EOF)
                    tokenizer = new StringTokenizer(lineBuffer, " ");
            }
            // key1, key2 로 해슁한 index 에 node 추가한다
            Prefix p = MakePrefix(key1, key2);
            long index = Hash(key1, key2);
            index = Math.abs(index);

            // 노드가 없다면 처음으로 추가
            if(htable[(int)index]==null){
                Node newN = MakeNode(p);
                htable[(int)index] = newN;
                // suffix 가 있다면 추가
                if(suffix != null){
                    Suffix s = MakeSuffix(suffix);
                    htable[(int)index].data.suf = s;
                    htable[(int)index].data.sumFreq++;
                }
            }
            // 노드가 존재한다면 연결
            else{
                Node head = htable[(int)index];
                Node before = null;
                boolean hasSamePrefix = false;
                while(head != null){
                    // 생성된 prefix가 기존의 prefix들 중 일치한다면
                    if(head.data.pword1.equals(key1) && head.data.pword2.equals(key2)){
                        // suffix 입력됬으면 추가
                        if(suffix != null){
                            Prefix nowP = head.data;
                            boolean hasSameSuffix = false;
                            // suffix 처음 생성시
                            if(nowP.suf == null){
                                Suffix tmpS = MakeSuffix(suffix);
                                nowP.suf = tmpS;
                                nowP.sumFreq++;
                            }
                            // suffix 존재시 연결
                            else{
                                Suffix beforeS = null;
                                Suffix tmpS = nowP.suf;
                                while(tmpS != null){
                                    // 동일한 suffix 존재시 freq 상승
                                    if(tmpS.sword.equals(suffix)){
                                        tmpS.freq++;
                                        nowP.sumFreq++;
                                        hasSameSuffix = true;
                                        break;
                                    }
                                    beforeS = tmpS;
                                    tmpS = tmpS.next;
                                }
                                // 여기까지오면 동일한 suffix 없으므로 추가
                                if(!hasSameSuffix){
                                    Suffix newS = MakeSuffix(suffix);
                                    nowP.sumFreq++;
                                    beforeS.next = newS;
                                }
                            }
                        }
                        hasSamePrefix = true;
                        break;
                    }
                    before = head;
                    head = head.next;
                }
                // 여기까지 오면 동일한 prefix 없으므로 추가
                if(!hasSamePrefix){
                    // 여기서도 suffix 입력됬으면 추가해줘야함
                    if(suffix != null){
                        Suffix newS = MakeSuffix(suffix);
                        p.suf= newS;
                        p.sumFreq++;
                    }
                    Node newNode = MakeNode(p);
                    before.next = newNode;
                }
            }
            //token = tokenizer.nextToken(); while문 맨 앞으로 옮김
            if(EOF)
                break;
        }
        //System.out.println("File Read.");
        return true;
    }
    public void PrintHashTable(){
        System.out.println("Print HashTable");
        Node node;
        for(int i = 0 ;i < NHASH ; i++){
            node = htable[i];
            if(node == null)
                continue;
            Prefix p = node.data;
            System.out.print("#" + i + ": Prefix: " + p.pword1 + ", " + p.pword2 + "\t\t");
            System.out.print("Suffix: ");
            Suffix s = p.suf;
            while(s != null){
                System.out.print(s.sword + "(" + s.freq + "), ");
                s = s.next;
            }
            System.out.println();
        }
    }
    public long Hash(String key1, String key2){
        long h = 0;
        String p = key1;
        for(int i = 0; i < p.length(); i++){
            int chr = Character.getNumericValue(p.charAt(i));
            h = MULTIPLIER * h + chr;
        }
        p = key2;
        for(int i = 0; i < p.length(); i++){
            int chr = Character.getNumericValue(p.charAt(i));
            h = MULTIPLIER * h + chr;
        }
        return h % NHASH;
    }
    public Prefix MakePrefix(String key1, String key2){
        Prefix p = new Prefix();
        p.pword1 = key1;
        p.pword2 = key2;
        p.sumFreq = 0;
        p.suf = null;
        return p;
    }
    public Suffix MakeSuffix(String suffix){
        Suffix s = new Suffix();
        s.sword = suffix;
        s.freq = 1;
        s.next = null;
        return s;
    }
    public Node MakeNode(Prefix p){
        Node newNode = new Node();
        newNode.data = p;
        newNode.next = null;
        return newNode;
    }
    public void PrintFakeText(){
        Random rand = new Random();
        int randIndex;
        do {
            randIndex = Math.abs(rand.nextInt()) % NHASH;
        }while(htable[randIndex] == null);
        System.out.print(htable[randIndex].data.pword1 + " " + htable[randIndex].data.pword2);
        MakeFakeText(htable[randIndex].data, 0);
    }
    public int MakeFakeText(Prefix prefix, int count){
        if(prefix == null || count == 1000)
            return -1;
        Suffix randSuffix;
        randSuffix = GetRandomSuffix(prefix);
        if(randSuffix == null)
            return -1;
        System.out.print(randSuffix.sword+ " ");
        Prefix nextPrefix = FindPrefix(prefix.pword2, randSuffix.sword);
        return MakeFakeText(nextPrefix, count+1);

    }
    public Suffix GetRandomSuffix(Prefix p){
        Random rand = new Random();
        int i = 0;
        int randNum, sequenceNum = 0;
        Vector weightArray = new Vector();
        Suffix suf = p.suf;
        if(suf == null || p.sumFreq == 0)
            return null;
        while(suf != null){
            if(i == 0){
                // 첫 suffix 의 freq는 그냥 추가
                weightArray.add(suf.freq);
                suf = suf.next; i++;
                continue;
            }
            // 현재 suffix의 freq와 이전이 값들을 누적하여 추가
            weightArray.add(suf.freq + (int)weightArray.elementAt(i-1));
            suf = suf.next; i++;
        }
        randNum = rand.nextInt() % p.sumFreq;
        for(int j = 0 ; j < weightArray.size() ; j++){
            // 처음으로 randNum 이 누적 freq보다 작거나 같은 suffix 찾는다
            if(randNum <= (int)weightArray.elementAt(j)){
                sequenceNum = j;
                break;
            }
        }
        // sequenceNum 번째로 나오는 suffix 찾아서 반환
        suf = p.suf;
        while(sequenceNum > 0){
            suf = suf.next;
            sequenceNum--;
        }
        return suf;

    }
    public Prefix FindPrefix(String key1, String key2){
        long index = Hash(key1, key2);
        index = Math.abs(index);
        int id = (int)index;
        Node n = htable[id];
        while(n != null){
            // 주어진 key와 같은 prefix 찾으면 반환
            if(n.data.pword1.equals(key1) && n.data.pword2.equals(key2)){
                return n.data;
            }
            n = n.next;
        }
        return null;
    }
}
