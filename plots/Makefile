all: plotdata plots

clean:
	rm -rf *.log *.aux *.bbl *.blg .toc *.out *.toc
	rm -rf paper.pdf

plotdata: construction.tex data/*
	./createAxis.sh
	sqlplot-tools construction.tex
	sqlplot-tools queries.tex

plots: construction.tex queries.tex data/*
	pdflatex plots.tex
	bibtex plots
	pdflatex plots.tex
	pdflatex plots.tex
