JFLAGS = -g
JARFLAGS = -cp .:jsoup-1.9.2.jar
JC = javac
JR = java

sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f *.class

run :
	@ # echo $(basename $(classes))
	@ # $(JR) $(JARFLAGS) $(basename $(classes))
	@ $(JR) $(JARFLAGS) Test

%.class : %.java
	$(JC) $(JFLAGS) $(JARFLAGS) $<