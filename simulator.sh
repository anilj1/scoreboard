#! /bin/sh

echo $0 $1 $2 $3 $4 $5

if test $# -lt 5; then
    echo
    echo "Not enough arguments"
    echo "Usage: simulator inst.txt data.txt reg.txt config.txt result.txt"
    echo
    echo "Please ensure the order of files for correct results"
    echo
    exit 1
fi

export CLASSPATH="./lib:./bin/com/sb/core/config/:./bin/com/sb/core/cpu/:./bin/com/sb/core/engine/:./bin/com/sb/core/fu/:./bin/com/sb/core/inst/:./bin/com/sb/core/memory/:./bin/com/sb/core/prog/:./bin/com/sb/core/register/:./bin/com/sb/parser/config/:./bin/com/sb/parser/data/:./bin/com/sb/parser/inst/:./bin/com/sb/parser/registers/:./bin/com/sb/writer/output/"

echo $CLASSPATH

#java -jar SB.jar com.sb.core.engine.ScoreBoard.class $1 $2 $3 $4 $5
java -jar SB.jar $1 $2 $3 $4 $5
