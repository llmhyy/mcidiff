# MCIDiff
This project is built for computing differences of multiple clone instances. The differencing result is able to regard syntactic boundary.

![Snapshot of MCIDiff](/diagram/screen.png?raw=true "Snapshot of MCIDiff")

## Eclipse Plugin vs Oridinary Java Application 
The project is built as an Eclipse plugin by default as MCIDiff is designed for being integrating into SE tools. Building tools in terms of Eclipse plugin is a mainstream in SE community. If you want to build the project into a common Java project, you can replace the pom.xml by the pom-application.xml file.

## Example
Examples on how to use MCIDiff API are listed in the test package under src/test directory. MCIDiff provide two alternatives to diff the cloned code, (1) token-based diff, which report diff result in terms of tokens; and (2) token-sequence-based diff, which report diff result in terms of token sequence regarding the syntactic boundary. The token-based diff technique is proposed in our ICSE'14 work and the token-sequence-based diff technique is proposed in our FSE'15 work. Please check more details in the Citation Section.

## Line Delimiter 
The differencing result of MCIDiff contains line information such as token position and token offset. Generally, you may need to change the line delimiter into Windows form (i.e., \r\n). Otherwise, the line information get incorrect.

## Citation
If you need to reference our technique, please use the following citations:

Yun Lin, Xin Peng, Zhenchang Xing, Diwen Zheng, and Wenyun Zhao. 2015. Clone-based and interactive recommendation for modifying pasted code. In Proceedings of the 2015 10th Joint Meeting on Foundations of Software Engineering (ESEC/FSE 2015). ACM, New York, NY, USA, 520-531. DOI=http://dx.doi.org/10.1145/2786805.2786871

Yun Lin, Zhenchang Xing, Yinxing Xue, Yang Liu, Xin Peng, Jun Sun, and Wenyun Zhao. 2014. Detecting differences across multiple instances of code clones. In Proceedings of the 36th International Conference on Software Engineering (ICSE 2014). ACM, New York, NY, USA, 164-174. DOI=http://dx.doi.org/10.1145/2568225.2568298

## Contact
If you have any problem on using our code, please feel free to contact me by: llmhyy@gmail.com or linyun@fudan.edu.cn. You can also contact Prof. Xin Peng (pengxin@fudan.edu.cn) or Prof. Zhenchang Xing (zcxing@ntu.edu.sg) for more information.


