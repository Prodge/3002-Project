JFLAGS = -g -Xlint
JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	LoggerOutput.java\
	sslconnection.java\
	client.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
