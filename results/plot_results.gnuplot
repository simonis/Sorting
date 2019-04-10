# see: http://brunogirin.blogspot.com/2010/09/memory-usage-graphs-with-ps-and-gnuplot.html
#      https://medium.com/@chamilad/basic-process-metrics-collection-and-visualization-in-linux-3d0fce3eeb87
#      https://dzone.com/articles/monitoring-process-memorycpu-usage-with-top-and-pl

set terminal svg size 800,600
set output "results.svg"
set title "Quicksort on a 20 cores / 40 hyperthreads machine"
set ylabel "milliseconds"
set xlabel "Nr. of elements"
set xtics rotate by 45 right
set decimal locale "de_DE.UTF-8"
set format x "%'g"

plot "results_ser.txt" using 2:xticlabels(1) with linespoints lw 2 title "Serial", \
     "results_par2.txt" using 2 with linespoints lw 2 title "2 Threads", \
     "results_par4.txt" using 2 with linespoints lw 2 title "4 Threads", \
     "results_par8.txt" using 2 with linespoints lw 2 title "8 Threads", \
     "results_par16.txt" using 2 with linespoints lw 2 title "16 Threads", \
     "results_par32.txt" using 2 with linespoints lw 2 title "32 Threads", \
     "results_par64.txt" using 2 with linespoints lw 1 lt rgb "#000000" title "64 Threads"
     