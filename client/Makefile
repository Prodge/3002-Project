JC = javac

.SUFFIXES: .java .class

.java.class:
	$(JC) $*.java

CLASSES = \
	sslconnection.java\
	client.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
