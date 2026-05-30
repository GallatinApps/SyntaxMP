package com.gallatinapps.syntaxmp.demo.model.samples

internal val PerlSample = """
    use strict;
    use warnings;
    use feature 'say';

    my ${'$'}mention_re = qr/\B\@([A-Za-z0-9_]+)/;

    sub reading_minutes {
        my (${'$'}words) = @_;
        my ${'$'}minutes = int((${'$'}words + 219) / 220);
        return ${'$'}minutes < 1 ? 1 : ${'$'}minutes;
    }

    for my ${'$'}note (@notes) {
        next if ${'$'}note->{archived};
        my ${'$'}owner = ${'$'}note->{title} =~ ${'$'}mention_re ? ${'$'}1 : 'local';
        my ${'$'}label = sprintf "%s - %dm - @%s",
            ${'$'}note->{title},
            reading_minutes(${'$'}note->{words}),
            ${'$'}owner;
        say ${'$'}label;
    }
""".trimIndent()
