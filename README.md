# MCIDiff
This project is built for computing differences of multiple clone instances.

The project is built as an Eclipse plugin by default as MCIDiff replies on the JDT API to parse syntactic information of program. If you want to build the project into a common Java project, you can replace the pom.xml by the pom-application.xml file.

Two examples on how to use MCIDiff API are listed in the test package under src/test directory.

The differencing result of MCIDiff contains line information such as token position and token offset. Generally, you may need to change the line delimiter into Windows form. Otherwise, the line information get incorrect.

If you need to reference our technique, please use the following citations:

Yun Lin, Xin Peng, Zhenchang Xing, Diwen Zheng, and Wenyun Zhao. 2015. Clone-based and interactive recommendation for modifying pasted code. In Proceedings of the 2015 10th Joint Meeting on Foundations of Software Engineering (ESEC/FSE 2015). ACM, New York, NY, USA, 520-531. DOI=http://dx.doi.org/10.1145/2786805.2786871

Yun Lin, Zhenchang Xing, Yinxing Xue, Yang Liu, Xin Peng, Jun Sun, and Wenyun Zhao. 2014. Detecting differences across multiple instances of code clones. In Proceedings of the 36th International Conference on Software Engineering (ICSE 2014). ACM, New York, NY, USA, 164-174. DOI=http://dx.doi.org/10.1145/2568225.2568298

If you have any problem on using our code, please feel free to contact me by: llmhyy@gmail.com or linyun@fudan.edu.cn


