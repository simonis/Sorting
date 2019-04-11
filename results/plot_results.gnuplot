# see: http://brunogirin.blogspot.com/2010/09/memory-usage-graphs-with-ps-and-gnuplot.html
#      https://medium.com/@chamilad/basic-process-metrics-collection-and-visualization-in-linux-3d0fce3eeb87
#      https://dzone.com/articles/monitoring-process-memorycpu-usage-with-top-and-pl
#
# usage: gnuplot -e "OUT_FILE='results.svg'" -e "IN_FILES='results_*.txt'" plot_results.gnuplot
#

if (!exists("IN_FILES")) IN_FILES="results_*.txt"
if (!exists("OUT_FILE")) OUT_FILE="results.svg"
if (!exists("TITLE")) TITLE="Quicksort on a 20 cores / 40 hyperthreads machine"

set terminal svg size 800,600
set output OUT_FILE
set title TITLE
set ylabel "milliseconds"
set xlabel "Nr. of elements"
set xtics rotate by 45 right
set decimal locale "de_DE.UTF-8"
set format x "%'g"

list = system("echo $(ls -v ".IN_FILES.")")
plot for [f in list] f using 2:xticlabels(1) with linespoints lw 2 title columnhead(1)

#plot "results_ser.txt" using 2:xticlabels(1) with linespoints lw 2 title columnhead(1), \
#     "results_par2.txt" using 2 with linespoints lw 2 title columnhead(1), \
#     "results_par4.txt" using 2 with linespoints lw 2 title columnhead(1), \
#     "results_par8.txt" using 2 with linespoints lw 2 title columnhead(1), \
#     "results_par16.txt" using 2 with linespoints lw 2 title columnhead(1), \
#     "results_par32.txt" using 2 with linespoints lw 2 title columnhead(1), \
#     "results_par64.txt" using 2 with linespoints lw 1 lt rgb "#000000" title columnhead(1)
     