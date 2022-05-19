# KargaMobile

Main code repository for the KargaMobile system.

## Usage

Currently, system requires for the sequence (.fastq) and reference (.fasta) files to be in the phone or in attached memory.

## Configurations

### Interface Enabled

1. K-value - determines the size of the k-mer comparison. (default: 17).  
2. Coverage threshold - once the whole document has been mapped, it filters to show only genes that are above the threshold.

### Code Enabled

In the Global class, testing features and analytics can be enabled/disabled.

## Functionality

System executes the KARGAM gene mapping process in a foreground thread, which communicates back to the application when the process is completed.   
Once completed, the system stores a .CSV file with the results in the default app folder (/android/data/org.ruizlab.phoni.kargamobile/).  
The app also shows the list of identified genes, a graph with the different classes, and the .CSV export functionality.  

When activated, the system executes another foreground thread with analytics, recording total wall/CPU time, max/average RAM, and max/average temperature. Analytics results are stored in a separate .CSV file in the same default app folder.  

## Support

Contact a.barquero@ruizlab.org
