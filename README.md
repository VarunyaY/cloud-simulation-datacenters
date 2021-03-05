**Homework 1**

**Varunya Yanamadala**

Computer Science Master's Student

University of Illinois at Chicago 

**Description**: A comparative study on different characteristics of a cloud provider by simulations and to simulate datacenters with 4 four types of service models.


Summary of the different parts of the homework:

**Part 1:**
To start of the homework, started with setting up the cloudsim plus examples from the git repo. I then considered the different examples to be the base for my simulations. I will discuss more in the next parts on which examples helped me the most and how I ended up referring the documentation. I chose to do the homework in scala and hence imported the cloudsim plus libraries using sbt and tested the integration by running the "BasicFirstExample" from the git repo.

**Part 2 and 3:**
By exploring the various allocation policies and scheduler mechanisms at Host and VM level, brought up a basic simulation with a set of characteristics as mentioned earlier. The file "Simluation_2and3.scala" has this part implemented.

**Part 4:**
With the basic simulation in place from the previous step, deep dived a little more into which characteristics could land up in a better model. The files "Simulation_4a.scala", "Simulation_4b.scala","Simulation_4c.scala","Simulation_4d.scala" has these implementations. The different implementations in each file are explained at the start of each of these files and the key observations are explained in the later part of this note.

**Part 5:**
In this simulation user input is taken and the datacenter which supports the model(as per user requests) is created. 


**Setup of the repo in IntelliJ IDE**
Clone the git repo using: git clone https://varunya@bitbucket.org/cs441-fall2020/varunya_yanamadala_hw1.git onto IntelliJ IDE with scala plugin installed.
 1. In IntelliJ this can be done through File-> New-> Project from Version Control.
 2. Select Version control as Git, provide the above git URL and the directory path to clone it.
 3. Click clone. 


**Observations from Part 2, 3 and 4 simulations:**
Several characteristics were considered to run the simulations to judge on which could be a better model for a customer. Each of the simulation is presented below.

**Simulation1**(the basic simulation implemented in the file Simulation_2and3.scala): It has the structure built with components to create a broker, datacenter, hosts, virtual machines, to create cloudlets and to assign them appropriately with the following characteristics. The number of cores/processing elements required for each cloudlet, number of cores/processing elements provided by each host, and that required by a vm with other parameters are configured in the corresponding conf file.
Of the different cloudlet UtilizationModels I considered the full utilization model to tally the numbers between cloudlets, hosts and vms for the configured values and cloudlet scheduler policy as time shared to observe the start time and end time of the cloudlets.

Below are the simulations with the configuration file of the previous one but with different allocation policies at host/vm levels, scheduler policies and utilization models. The below four simluations are executed in the files Simulation_4a, Simulation_4b, Simulation_4c and Simulation_4d.
**Simulation2** - Simple VM Allocation Policy(WorstFit), Cloudlet TimeShared Policy and Cloudlet utilization of 50%

**Simulation3** - RoundRobin VM Allocation Policy, Cloudlet TimeShared and Cloudlet Full Utilization model

**Simulation4** - RoundRobin VM Allocation Policy, Cloudlet TimeShared policy  and Cloudlet Dynamic Utilization model of 80%

**Simulation5** - Best fit VM Allocation Policy, CLoudlet TimeShared and Cloudlet Dynamic Utilization model of 80%/50%

**Config parameters**
The following parameters are configured for the simulations in the correspodning conf files.
Number of VMs
Number of Cloudlets 
Host's RAM
Host's MIPS
Host's Storage
Host's Bandwidth
Host's PEs
Datacenter's number of hosts
Datacenter's cost per second, cost per memory, cost per storage, cost per bandwidth
Cloudlet's PEs
Cloudlet Length
Vm's number of PEs
Vm's MIPS
Vm's Ram
Vm's Bandwidth

Effect of Utilization model when the rest of the parameters are kept constant: As the cloudlet utilization reaches 1, the time it takes to finish decreases upto a point. So, the effect is linear between utilization model and execution time.

Timeshared vs Spaceshared:

When the characteristics like number of VMs, number of PEs required by a VM, number of PEs required by a cloudlet, number of PEs in a host are kept constant and if the number of hosts are lesser in number than the number of VMs, there is a significant difference in the total execution time of the cloudlets i.e in space shared not all cloudlets took the same time to execute and hence, the overall cost was lesser for space shared policy. With that said, imagine a task which doesnt have to wait provided with its space. Then Spaceshared policy is a better bet. But, needless to say, the cloudlet would have to wait endlessly for the current tasks to finish and hence in real world scenario a time shared policy might be a better bet as the time is shared in a roundrobin policy between cloudlets and VMs. Hence, depending on the demand requested from the customer different price ranges could be provided to assign the cloudlet policy.

Different VM Allocation policies:

A random fit might not always be fruitful given that it just find a host randomly. A round robin would be the best fit for any implementations as it find the next suitable host with resources to assign the VM.

When the RAM of the VM is decreased than the HOST, the cloudlets take a longer time to execute. Also, the MIPS capacity of the host has to be larger than the capacity of the VM.

**Implementation of Part 5:**
To decide the type for service that has to be offered to the customer, questions are prompted to the user to decide on the model. Based on the level of control customer requests, the following were checked to allocate the service below are the factors by which a service is allocated.
1. If customer chooses to give operating system then the service is IaaS.
2. If customer chooses the cloudletscheduler type - Paas
3. Cloudlet PEs and number of cloudlets - Saas
4. Cloudlet length - Faas

 
In the above scenarios different datacenters are configured and the service is allocated to the dedicated datacenter as per the service requested.

Test cases are added in Simulation_2and3 and Simluation_part5 to check if the objects are being created with the requested parameters. The test cases correspond to the basic simulation in Simulation_2and3 and Simulation_part5.

**Limitations**
Few gaps where this simulation could be improved is by considering a fallback datacenter in case VMs are created but waiting for hosts to get freed in a datacenter. For this MyBroker was implemented but, the process fails at creating the VMs itself.

Though cost at memory, bandwidth, storage are considered the other considerations of the time taken for the write operations onto the disk, maintenance of the datacenters, optimizations of the VM usage through migration aren't considered.


