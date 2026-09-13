# NYC Taxi Data Analysis

An end-to-end Big Data analysis project examining New York City taxi trip records using Hadoop MapReduce and Apache Pig to evaluate trip metrics, passenger patterns, and fare distributions.

---

## 📁 Dataset

Due to GitHub's file size limits, the primary raw dataset is hosted externally.

* **Download Dataset:** [Google Drive Dataset Link](https://drive.google.com/file/d/1EOmf3ZsG6Rn18bma8hb0cJiNHRSfr6Zh/view?usp=drive_link)
* **Format:** CSV
* **Description:** NYC Yellow Taxi trip records covering pickup/dropoff timestamps, passenger counts, trip distances, and fare breakdowns.

---

## 📂 Project Structure

```text
NYC-Taxi-Data-Analysis/
│   ├── mapreduce_src/          # MapReduce Java source files (Mappers, Reducers, Drivers)
│   ├── mapreduce_classes/      # Compiled class files
│   ├── mapreduce_jars/         # Executable JAR binaries for Hadoop execution
│   ├── pig_scripts/            # Apache Pig Latin query scripts (.pig)
│   ├── report_results/         # Aggregated job outputs and metrics
│   └── output_screenshots/     # Execution verification and output terminal screenshots
│   └──NYC_Taxi_Data_Analysis_Report.docx # Detailed project report & methodology
│   └──NYC_Taxi_Data_Analysis_Report.pdf # Detailed project report & methodology pdf format
├── .gitignore                  # Git ignore rules for builds, temp files, and raw data
└── README.md                   # Project documentation
