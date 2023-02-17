#!/bin/bash

function writePlots() {
    dataset=$1
    additional=$2
    cat <<EOF >> construction.tex
\\begin{tikzpicture}
    \\begin{axis}[
        title={$dataset},
        plotCompetitorConstruction,
        legend to name=legendCompetitors,$additional
      ]
      \\addplot[no marks,veryLightGrey,line width=2pt,forget plot,sharp plot] coordinates { (13,0) (13,100) };
      %% MULTIPLOT(name|ptitle|attr)
      %%   SELECT
      %%     MIN(bitsPerElement, 13) as x,
      %%     0.001*N/constructionTimeMilliseconds as y,
      %%     name,
      %%     attr,
      %%     paper_name AS ptitle
      %%   FROM competitors
      %%   JOIN competitorNames ON name = code_name
      %%   WHERE dataset="$dataset"
      %%   ORDER BY name,x
    \\end{axis}
\\end{tikzpicture}
EOF
    cat <<EOF >> queries.tex
\\begin{tikzpicture}
    \\begin{axis}[
        title={$dataset},
        plotCompetitorQueries,
        legend to name=legendCompetitorsQueries,$additional
      ]
      \\addplot[no marks,veryLightGrey,line width=2pt,forget plot,sharp plot] coordinates { (13,0) (13,100) };
      %% MULTIPLOT(name|ptitle|attr)
      %%   SELECT
      %%     MIN(bitsPerElement, 13) as x,
      %%     0.001*numQueries/queryTimeMilliseconds as y,
      %%     name,
      %%     attr,
      %%     name AS title,
      %%     paper_name AS ptitle
      %%   FROM competitors
      %%   JOIN competitorNames ON name = code_name
      %%   WHERE dataset="$dataset"
      %%   ORDER BY name,x
    \\end{axis}
\\end{tikzpicture}
EOF
}

echo "% IMPORT-DATA competitors data/competitors.txt" > construction.tex
echo "% IMPORT-DATA competitorNames data/__competitorNames.txt" >> construction.tex
echo "% IMPORT-DATA competitors data/competitors.txt" > queries.tex
echo "% IMPORT-DATA competitorNames data/__competitorNames.txt" >> queries.tex

writePlots "trec-text.terms"
writePlots "trec-title.terms" "ylabel={},yticklabels={,,}"
writePlots "uk-2007-05.urls" "ylabel={},yticklabels={,,}"

