# pudding
An attempt of caching frequently used files into RAM to speed up programs 
that do not do so already.

#### Why make this?
Phantasy Star Online 2 (PSO2) is a great MMO that I love playing. There's 
one glaring issue with PSO2 though - it absolutely sucks for caching stuff.
My assumption is that it does next to no caching. As far as I can tell, two 
characters using the same clothing incurs 2 hits for each resource the
clothes use (mesh, texture, normal map, etc.) In theory, by caching the file 
used to store such elements into memory, we should have reduced latency,
as RAM IOPs are much greater than any SSD.

#### An attempt?
Yes, an attempt. In reality, due to a myriad of factors, I (author) expect
the speed up to be negated entirely (and to approach into being a deficit).
In no particular order:

- Java and Foreign Functions - For long computational work, Java and native
libraries perform just as you'd expect: tremendous speed-ups due to
just being able to run native, compiled code. However, our use for the JNR
API is to access WinFsp to create a FUSE. Since a lot of operations on file
systems tend to be small (i.e read only a part of data, get name and perms),
this ends up leading to a lot of wasted speed as talking between JVM and 
native code tends to incur a lot of copying (anyone whose worked with JNI
can probably talk your ear off about this). Not really solvable without
resorting to porting to a native language.

- Poor caching algorithm - To begin, whenever we request a file, we generate
a cache entry that has a frequency counter. We add this entry onto a "rarely 
used" list, which serves to be a "we can discard this randomly" list. The other 
list serves as a list of "frequently used" items - those that should be kept
as long as possible. Whenever we access the same file, we increment the freq.
counter and try to "bubble" the entry upwards. If the entry reaches the end,
it is now a candidate for being promoted into "frequently used". Should it
succeed (simply check if the item's frequency is greater than the lowest of
"frequently used"), we swap the two elements. The problem arises from a lack
of demotion (items in frequently used never leave) and generally bad autosorting.
Solvable by "writing better code", but I feel the problem is much deeper than
that.

- Java - Lets not beat around the bush. There's a lot of overhead when running 
Java applications (namely, memory overhead from having the JVM). It isn't
exactly the most "performant" language due to waiting around for the JIT to
kick in and _maybe_ produce optimal instructions. GraalVM could potentially
solve this, but then we'd run into Graal + FFI issues. Similarly to the issue
with Java and foreign functions, this is only really solvable by migrating
to a native language.

#### Deep Lore
"Pudding" is the name for the NPC that hands out collection files on JP. 
Though, they changed her name to "Prin" for the NA release. Very unfortunate.