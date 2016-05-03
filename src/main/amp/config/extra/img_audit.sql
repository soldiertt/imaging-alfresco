-- POSTGRESQL
create table img_audit (
	audit_id serial primary key, 
	uname varchar(50) not null, 
	cdate timestamp not null, 
	docuuid varchar(50) not null, 
	doctype varchar(30), 
	frombox varchar(30) not null, 
	destination varchar(20) not null);
	
create index audt_m1_uuid on img_audit(docuuid);
create index audt_m2_uname on img_audit(uname);
