grammar IRC;

// IRC MAIN RULES
server_response
    : DLIMIT
    ( server_response_long
    | server_response_short
    | server_response_special)
    | server_response_error
    ;

server_response_long
    : server WHITESPACE code WHITESPACE nick
    ;

server_response_short
    : nick '!' user '@' server
    ;

server_response_special
    : server WHITESPACE code WHITESPACE '*' WHITESPACE nick
    ;

server_response_error
    : ':'? 'ERROR' WHITESPACE DLIMIT
    ;

message
    : (WORD | '!' | WHITESPACE | INTEGER)+
    ;

text
    : (WORD | WHITESPACE | INTEGER)+
    ;

target
    : channel
    | nick
    | '*'
    ;

server
    : (WORD
    | INTEGER
    | '-'
    | '.')+
    ;

version
    : (WORD
    | WHITESPACE
    | INTEGER
    | '-'
    | '.')+
    ;

code
    : INTEGER
    ;

nick
    : (WORD | INTEGER)+?
    ;

nicknames
    : '@' (nick | WHITESPACE)+
    ;

user
    : (WORD | INTEGER)+
    ;

fullname
    : (WORD | WHITESPACE)+
    ;

channel
    : '#' (WORD
    | '-')+
    ;

command
    : WORD
    ;

date
    : (WORD
    | INTEGER
    | WHITESPACE
    | ':'
    | '-')+
    ;

// IRC SPECIAL RULES

/* === SERVER RESPONSE === */

response
    : private_message
    | notice
    | no_such_nick_channel
    | cannot_send_to_channel
    | no_motd
    | nickname_in_use
    | pong
    | who_is_user
    | who_is_server
    | end_of_who_is
    | unknown_command
    | welcome
    | your_host
    | created
    | my_info
    | luser_client
    | luser_op
    | luser_unknown
    | luser_channel
    | luser_me
    | motd_start
    | motd
    | end_of_motd
    | quit
    | namelist
    | name_reply
    | end_of_names
    | part
    | not_on_channel
    | topic
    | no_topic
    | list
    | listend
    | who
    | end_of_who
    | nick_reply
    ;

pong
    : 'PONG'
    ;

quit
    : server_response 'Closing Link' DLIMIT WHITESPACE server WHITESPACE '(' message ')'
    ;

nick_reply
    : server_response WHITESPACE 'NICK' WHITESPACE DLIMIT nick
    ;

/* === WELCOME === */

welcome
    : server_response WHITESPACE DLIMIT 'Welcome to the Internet Relay Network' WHITESPACE server_response_short
    ;

your_host
    : server_response WHITESPACE DLIMIT 'Your host is' WHITESPACE server ',' WHITESPACE 'running version' WHITESPACE version
    ;

created
    : server_response WHITESPACE DLIMIT 'This server was created' WHITESPACE date
    ;

my_info
    : server_response WHITESPACE server WHITESPACE version WHITESPACE 'ao mtov'
    ;

/* === LUSER === */

luser_client
    : server_response WHITESPACE DLIMIT 'There are' WHITESPACE INTEGER WHITESPACE 'users and' WHITESPACE INTEGER WHITESPACE 'services on' WHITESPACE INTEGER WHITESPACE 'servers'
    ;

luser_op
    : server_response WHITESPACE INTEGER WHITESPACE DLIMIT 'operator(s) online'
    ;

luser_unknown
    : server_response WHITESPACE INTEGER WHITESPACE DLIMIT 'unknown connection(s)'
    ;

luser_channel
    : server_response WHITESPACE INTEGER WHITESPACE DLIMIT 'channels formed'
    ;

luser_me
    : server_response WHITESPACE DLIMIT 'I have' WHITESPACE INTEGER WHITESPACE 'clients and' WHITESPACE INTEGER WHITESPACE 'servers'
    ;

/* === MOTD === */

motd_start
    : server_response WHITESPACE DLIMIT '-' WHITESPACE server WHITESPACE 'Message of the day' WHITESPACE '-'
    ;

motd
    : server_response WHITESPACE DLIMIT '-' WHITESPACE message
    ;

end_of_motd
    : server_response WHITESPACE DLIMIT 'End of MOTD command'
    ;

/* === CHANNELS's === */

namelist
    : server_response WHITESPACE 'JOIN' WHITESPACE channel
    ;

name_reply
    : server_response WHITESPACE '=' WHITESPACE channel WHITESPACE DLIMIT nicknames
    ;

end_of_names
    : server_response WHITESPACE channel WHITESPACE DLIMIT 'End of NAMES list'
    ;

part
    : server_response WHITESPACE 'PART' WHITESPACE channel (WHITESPACE DLIMIT message)?
    ;

no_topic
    : server_response WHITESPACE channel WHITESPACE DLIMIT 'No topic is set'
    ;

topic
    : server_response WHITESPACE 'TOPIC' WHITESPACE channel WHITESPACE DLIMIT message
    | server_response WHITESPACE channel WHITESPACE DLIMIT (message | 'No topic is set')
    ;

/* === LIST === */

list
    : server_response WHITESPACE channel WHITESPACE INTEGER WHITESPACE DLIMIT message?
    ;

listend
    : server_response WHITESPACE DLIMIT 'End of LIST'
    ;

/* === PRIVMSG & NOTICE === */

private_message
    : server_response WHITESPACE 'PRIVMSG' WHITESPACE target WHITESPACE DLIMIT message
    ;

notice
    : server_response WHITESPACE 'NOTICE' WHITESPACE target WHITESPACE DLIMIT message
    ;

/* === WHOIS === */

who_is_user
    : server_response WHITESPACE nick WHITESPACE user WHITESPACE server WHITESPACE '*' WHITESPACE DLIMIT fullname
    ;

who_is_server
    : server_response WHITESPACE nick WHITESPACE server WHITESPACE DLIMIT version
    ;

end_of_who_is
    : server_response WHITESPACE nick WHITESPACE DLIMIT 'End of WHOIS list'
    ;

/* === WHO === */

who
    : server_response WHITESPACE target WHITESPACE user WHITESPACE server WHITESPACE server WHITESPACE nick WHITESPACE
        'H' '@'? WHITESPACE DLIMIT INTEGER WHITESPACE fullname
    ;

end_of_who
    : server_response WHITESPACE target WHITESPACE DLIMIT 'End of WHO list'
    ;

/* === ERROR's === */

no_such_nick_channel
    : server_response WHITESPACE target WHITESPACE DLIMIT ('No such channel' | 'No such nick/channel')
    ;

cannot_send_to_channel
    : server_response WHITESPACE channel WHITESPACE DLIMIT 'Cannot send to channel'
    ;

no_motd
    : server_response WHITESPACE DLIMIT 'MOTD File is missing'
    ;

nickname_in_use
    : server_response WHITESPACE DLIMIT 'Nickname is already in use'
    ;

unknown_command
    : server_response WHITESPACE command WHITESPACE DLIMIT 'Unknown command'
    ;

not_on_channel
    : server_response WHITESPACE channel WHITESPACE DLIMIT 'You\'re not on that channel'
    ;

// IRC LEXER RULES
DLIMIT
    : ':'
    ;

WORD
    : CHAR+
    ;

CHAR
    : 'a'..'z'
    | 'A'..'Z'
    ;

WHITESPACE
    : ' '
    | '\t'
    ;

INTEGER
    : INT+
    ;

INT
    : '0'..'9'
    ;

SPECIAL
    : '-'
    | '.'
    | '!'
    ;