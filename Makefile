JFLAGS = -g -Xlint:unchecked
JARFLAGS = -cp .:jsoup-1.9.2.jar:joda-time-2.9.4.jar
# ARFLAGS = -cp ".:./JarFiles/jsoup-1.9.2.jar:./JarFiles/joda-time-2.9.4.jar"
JC = javac
JR = java

sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f *.class

run :
	clear
	@ $(JR) $(JARFLAGS) $(basename $(classes))

test : 
	clear
	@ $(JR) $(JARFLAGS) $(basename $(classes)) < input.txt

%.class : %.java
	$(JC) $(JFLAGS) $(JARFLAGS) $<