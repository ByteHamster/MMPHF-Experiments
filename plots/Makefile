all: plotdata plots

clean:
	rm -rf *.log *.aux *.bbl *.blg .toc *.out *.toc
	rm -rf paper.pdf

plotdata: plots.tex data/*
	sqlplot-tools plots.tex

plots: plots.tex data/*
	pdflatex -interaction=batchmode plots.tex
	rm -rf *.log *.aux *.bbl *.blg .toc *.out *.toc
