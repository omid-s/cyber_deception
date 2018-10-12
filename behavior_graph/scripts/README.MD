# Audit runner script 

to collect data for behaviour graph tool a sub set of system calls are being collected. this helps with speedup.
an script has been designed to set up and run the auditd process. This script take the liberty of changing some auditd configurations by making changes to "/etc/audit/auditd.conf". 

to run : 

	$sudo bash doAudit.bash 