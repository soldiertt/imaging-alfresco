-- POSTGRESQL
create table img_audit_box (
	audit_id serial primary key, 
	uname varchar(50), 
	indate timestamp not null, 
	outdate timestamp, 
	docuuid varchar(50) not null, 
	doctype varchar(30), 
	docsource varchar(30) not null,
	doclettertype varchar(4),
	boxname varchar(30) not null);
	
create index audtbox_m1_uuid on img_audit_box(docuuid);
create index audtbox_m2_uname on img_audit_box(uname);
create index audtbox_m3_doctype on img_audit_box(doctype);
create index audtbox_m4_docsource on img_audit_box(docsource);
create index audtbox_m5_doclettertype on img_audit_box(doclettertype);
create index audtbox_m6_boxname on img_audit_box(boxname);