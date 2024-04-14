all: run

clean:
	rm -f out/MonteCarlo.jar

out/MonteCarlo.jar: out/parcs.jar src/MonteCarlo.java
	@mkdir -p temp
	@javac -cp out/parcs.jar -d temp src/MonteCarlo.java
	@jar cf out/MonteCarlo.jar -C temp .
	@rm -rf temp/

build: out/MonteCarlo.jar

run: out/MonteCarlo.jar
	@cd out && java -cp 'parcs.jar:MonteCarlo.jar' MonteCarlo $(WORKERS)
