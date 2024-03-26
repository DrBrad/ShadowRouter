ShadowRouter
=====

Shadow Router is a Kademlia/Mainline based anonymous router that utilizes onion like protocol and I/O splitting.

ShadowRouter Vs. TOR
-----
What seperates ShadowRouter apart from TOR is that we split Input and Output streams and our Certificate Authority system is very different. TOR's Certificate Authority is a set of Chord DHT nodes which are approved by TOR staff, this means that your relays could be unsafe and has a limited ammount of trusted Authorities (Decentralized). With ShadowRouter we base everything off of Kademlia, we start you off with 10 known trusted Nodes, similar to TORs Certificate Authority, except once you have joined the DHT you will get a substantial amount of nodes to choose from to relay to. Every message contains a UUID to stop Sybil attacks, it must contain a Public Key and a Signature to stop MITM and prevent Eclipse attacks. Unlike TOR, ShadowRouter offers switching between UDP and TCP for relays and allows for UDP messages to be sent and received IE full support of SOCKS5.

Hidden Services
-----

