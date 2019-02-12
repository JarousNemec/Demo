@echo off

if exist "d:\builds\code_review_statistics\quadient_cr_statistics.csv" del "d:\builds\code_review_statistics\quadient_cr_statistics.csv"
if exist "d:\builds\code_review_statistics\quadient_cr_statistics.pdf" del "d:\builds\code_review_statistics\quadient_cr_statistics.pdf"

for /d %%i in ("c:\tools\code-review\repos\*") do (git -C "%%i" pull & java -jar "c:\Program Files (x86)\Jenkins\workspace\CodeReview\build\libs\cr-statistics-0.1.jar" --repository "%%i" --statistics "d:\builds\code_review_statistics" --type "csv")

"d:\GMC\InspireDesigner12.0\InspireCLI.exe" "c:\Program Files (x86)\Jenkins\workspace\CodeReview\cr_reporter.wfd" -e PDF -o Output1 -f "d:\builds\code_review_statistics\quadient_cr_statistics.pdf" -difDataInputCSV "d:\builds\code_review_statistics\quadient_cr_statistics.csv" -nowarnings

echo Output PDF saved in "d:\builds\code_review_statistics"

Exit 0