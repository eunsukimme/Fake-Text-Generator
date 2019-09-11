# Fake-Text-Generator

마르코프 체인을 응용한 진짜같은 가짜 자연어 생성기 입니다.

## Getting Started

설치하고 실행하는 과정은 간단합니다. JAVA가 설치되어야 하며, 입력으로 주어질 긴 텍스트 파일이 필요합니다.

### Requirements

- JDK >= 8 (recommended)
- Command Line Tool(javac, java)

### Installation

먼저 해당 레포지토리를 클론합니다.

```
$ git clone https://github.com/eunsukimme/Fake-Text-Generator
```

그런 다음 클론한 폴더로 이동합니다.

```
$ cd Fake-Text-Generator
```

src 폴더에 있는 .java 파일을 컴파일하고 .class 파일을 out 폴더에 생성합니다.

```
$ javac src/*.java -d out
```

그런 다음 out 폴더로 이동한 후 MarkovChainClass 파일을 실행합니다.

```
$ cd out && java MarkovChainClass
```

마지막으로 입력으로 주어질 텍스트 파일의 이름을 상대경로로 전달합니다.(../harry.txt 또는 ../magicSchool.txt)

```
$ 읽을 파일 이름을 입력하시오(ex. harry.txt): ../harry.txt
```

정상적으로 실행되었다면 다음과 같은 가짜 텍스트를 출력합니다.

```
Harry toldher. She nipped his finger still more vigorously into his ear and rotated it.
Owing, no doubt, to a table, the largest and grandest building for miles around, the Riddle House.
'"S far as I can tell," said Mr. Weasley. "Better get cracking then." He pushed up the street.
"Why don't you show Harry where he's sleeping, Ron?" said Hermione from the war with a flourish and
read out large portions of their murderer ??for plainly, three apparently healthy people did not spring to life again.
(...생략)
```

또한 실행할 때 마다 새로운 가짜 텍스트를 출력합니다.

```
old house,he merely continued to twitch. "Reducio," Moody muttered, and the whole of Little Hangleron
still called it "the Riddle House," even though the weeds were starting to creep up to the suddenly silent pub that
a man called Peter, nicknamed Wormtail... and a great wave of his arm, which was propped against the mantelpiece,
slightly apart from Mr. Weasley. "Here, look." Mr. Diggory shout.
(...생략)
```

전체 결과는 아래 링크에서 확인할 수 있다\
[harry.txt(영어)](./fake_harry.md)\
[magicSchool.txt(한글)](./fake_magicSchool.md)

## How It Works

텍스트에서 모든 **연속된 두 단어의 쌍**과 **그 쌍에 이어서 바로 등장한 단어**의 목록을 찾아냅니다. 해당 목록에서 동일한 확률로 임의의 단어를 선택하여 연결하고,
새롭게 연결된 단어와 바로 이전 단어를 **연속된 두 단어의 쌍**으로 간주하고 위 과정을 반복합니다.

### Key Concept

다음과 같은 텍스트로 예를 들어 봅시다.\
\
_Show your flowcharts and conceal your tables and I will be
mystified. Show your tables and your flowcharts will be
obvious. [end]_\
\
먼저 **"연속된 두 단어의 쌍"**을 **prefix**라고 부르고, **“이어서 등장한 단어”**를 **suffix**라고 합시다. 위 텍스트에서 Prefix와 Suffix를 표로 정리하면 다음과 같습니다.

| PREFIX          | SUFFIX                     |
| --------------- | -------------------------- |
| Show your       | flowcharts(1), tables(1)   |
| your flowcharts | and(1), will(1)            |
| flowcharts and  | conceal(1)                 |
| and conceal     | your(1)                    |
| flowcharts will | be(1)                      |
| your tables     | and(2)                     |
| will be         | mystified.(1), obvious.(1) |
| be mystified.   | Show(1)                    |
| be obvious.     | [end]                      |
| tables and      | I(1), your(1)              |
| and I           | will(1)                    |
| ...             |                            |

예를 들어 *Show your*라는 prefix 다음에는 *flowcharts*라는 suffix와 *tables*라는 suffix가 각각 1번씩 등장한 적이 있습니다. 그리고 *your tables*라는 prefix 다음에는 *and*가 2번 등장한 적이 있습니다.\
\
즉 괄호 안의 숫자는 그 suffix가 해당 prefix 바로 다음에 등장한 횟수를 표시합니다. 마침표, 쉼표 등의 기호는 단어의 일부로 간주하고, 대소문자는 구분 합니다. 그리고[end]는 텍스트의 끝을 의미합니다. 이제 랜덤 텍스트를 만드는 Markov chain 알고리즘이 진행되는 과정을 살펴봅시다.

### Process

먼저 Show your를 출력합니다.\
\
_<u>Show your</u>_\
\
그런 다음 *Show your*의 suffix인 *flowcharts*와 _tables_ 중의 하나를 1/2의 확률로 선택합니다. 가령 *tables*가 선택되었다고 가정합시다. 그럼 *tables*를 출력하고\
\
_Show <u>your tables</u>_\
\
이제 가장 마지막으로 출력된 두 단어인 *your tables*가 새로운 prefix가 됩니다. 이 prefix에 대한 suffix는 *and*가 유일하므로 *and*를 출력합니다.\
\
_Show your <u>tables and</u>_\
\
이제 *tables and*가 prefix가 되고 *suffix*인 *I*와 _your_ 중에 하나를 1/2의 확률로 선택헙니다. **일반적으로는 *suffix*들을 등장 빈도에 비례하는 확률로 선택하게 됩니다.
가령 *I*의 등장 빈도가 3이고 *your*가 1이었다면 3/4의 확률로 *I*를, 1/ 4의 확률로 *your*를 선택해야 합니다.** 만약 *I*가 선택되었다면 *I*를 출력하고\
\
_Show your tables <u>and I</u>_\
\
이제 *and I*의 유일한 suffix인 *will*을 출력합니다.\
\
_Show your tables and <u>I will</u>_\
\
이런 식으로 계속하여 더이상 suffix가 존재하지 않거나 (즉 [end]에 도달하거나) 혹은 생성할 랜덤 텍스트의 길이 의 최대값에 도달하면 종료합니다.

## Building Blocks

위 구조는 하나의 Prefix가 여러개의 Suffix를 보유하고 있는 형태로, 다음과 같은 구조체로 나타낼 수 있습니다.

```
typedef struct suffix {
  char *sword;        // suffix인 단어
  int freq;           // 등장횟수
  Suffix *next;       // 다음 노드의 주소
} Suffix;
```

```
typedef struct prefix {
char *pword1;         // prefix를 구성하는 첫 단어
char *pword2;         // prefix를 구성하는 두 번째 단어
Suffix \*suf;         // suffix들의 연결리스트의 첫 번째 노드의 주소
int sumFreq;          // suffix들의 등장횟수(freq)의 합
} Prefix;
```

<img width="627" alt="Screen Shot 2019-09-11 at 9 22 28 AM" src="https://user-images.githubusercontent.com/31213226/64659266-bb4a9480-d475-11e9-9df5-0af7286111a2.png">\
\
위의 그림과 같은 구조를 가지는 여러 개의 Prefix객체들을 저장하기 위해서 해쉬 테이블을 사용합니다. 동일한 Prefix로 인한 충돌은 Suffix를 chaining함으로써 해결합니다. 즉, 해쉬 테이블의 각 칸은 하나의 연결리스트를 거느리고 있고, 그 연결리스트의 각 노드에는 하나의 Prefix 객체의 주소를 저장합니다. 그리고 해당 Prefix뒤로 Suffix들이 줄줄이 이어집니다.

```
#define NHASH 4093 // NHASH는 해쉬 테이블의 크기

typedef struct node {
  Prefix *data;
  Node *next;
} Node;

Node *htable[NHASH]; // 해쉬 테이블
```

<img width="702" alt="Screen Shot 2019-09-11 at 9 29 25 AM" src="https://user-images.githubusercontent.com/31213226/64659424-ad494380-d476-11e9-968e-46a91c910706.png">\
\
해쉬 함수는 Prefix를 구성하는 두 단어를 키(key)로 사용합니다.

```
#define MULTIPLIER 31
  unsigned int hash(char *key1, char *key2) {
    unsigned int h = 0;
    unsigned char *p;
    for (p = (unsigned char *)key1; *p != '\0'; p++)
        h = MULTIPLIER * h + *p;
    for (p = (unsigned char *)key2; *p != '\0'; p++)
        h = MULTIPLIER * h + *p;
    return h % NHASH;
  }
```

## Acknowledgments

입력으로 주어지는 텍스트 파일(.txt)에 포함된 문자들은 UNICODE 또는 ASCII 만 포함하여야 합니다.

## License

MIT
