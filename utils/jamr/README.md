JAMR - AMR Parser
=================

JAMR is a semantic parser and aligner for the [Abstract Meaning Representation](http://amr.isi.edu/). 

We have released [hand-alignments](docs/Hand_Alignments.md) for 200 sentences of the AMR corpus.

For the performance of the parser, see [docs/Parser_Performance](docs/Parser_Performance.md).

#Building

First checkout the github repository (or download the latest release):

    git clone https://github.com/jflanigan/jamr.git

JAMR depends on [Scala](http://www.scala-lang.org), [Illinois NER
system](http://cogcomp.cs.illinois.edu/page/download_view/NETagger) v2.7, tokenization scripts in
[cdec](https://github.com/redpony/cdec), and [WordNet](http://wordnetcode.princeton.edu/3.0/WordNet-3.0.tar.gz) for the
aligner. To download these dependencies into the subdirectory `tools`, cd to the `jamr` repository and run (requires
wget to be installed):

    ./setup

You should agree to the terms and conditions of the software dependencies before running this script.  If you download
them yourself, you will need to change the relevant environment variables in `scripts/config.sh`.  You may need to edit
the Java memory options in the script `sbt` and `build.sbt` if you get out of memory errors.

Source the config script - you will need to do this before running any of the scripts below:

    . scripts/config.sh

Run `./compile` to build an uberjar, which will be output to
`target/scala-{scala_version}/jamr-assembly-{jamr_version}.jar` (the setup script does this for you).

#Running the Parser

Download and extract model weights [models.tgz](http://cs.cmu.edu/~jmflanig/models.tgz) into the directory
`$JAMR_HOME/models`.  To parse a file (cased, untokenized, with one sentence per line) with the model trained on
LDC2014E41 data do:

    . scripts/config.sh
    scripts/PARSE.sh < input_file > output_file 2> output_file.err

The output is AMR format, with some extra fields described in [docs/Nodes and Edges
Format](docs/Nodes_and_Edges_Format.md) and [docs/Alignment Format](docs/Alignment_Format.md). To run the parser trained
on other datasets (such as the older LDC2013E117 data, or freely downloadable [Little
Prince](http://amr.isi.edu/download.html) data) source the config scripts `config_LDC203E41.sh`
or `config_Little_Prince.sh` instead.

#Running the Aligner

To run the rule-based aligner:

    . scripts/config.sh
    scripts/ALIGN.sh < amr_input_file > output_file

The output of the aligner is described in [docs/Alignment Format](docs/Alignment_Format.md).  Currently the aligner
works best for release r3 data (AMR Specification v1.0), but it will run on newer data as well.

#Hand Alignments

To create the hand alignments file, see [docs/Hand Alignments](docs/Hand_Alignments.md).

#Experimental Pipeline

The following describes how to train and evaluate the parser.  There are scripts to train the parser on various
datasets, as well as a general train script to train the parser on any AMR dataset.  More detailed instructions for
training the parser are in [docs/Step by Step Training](docs/Step_by_Step_Training.md).

To train the parser on LDC data or public [AMR Bank](http://amr.isi.edu/download.html) data, download the data .tgz file
into to `$JAMR_HOME/data/` and run one of the train scripts.  The data file and the train script to run for each of the datasets
is listed in the following table:

| Dataset | Date released | Size (# sents) | Script to run   | File to move to `data/` |
| --- | ---- | ---- | ---- | --- |
| [LDC2014T12](https://catalog.ldc.upenn.edu/LDC2014T12) | June 16, 2014 | 13,051 | `scripts/train_LDC2014T12.sh`    | `amr_anno_1.0_LDC2014T12.tgz` |
| LDC2014E41 | May 30, 2014 | 18,779 | `scripts/train_LDC2014E41.sh`    | `LDC2014E41_DEFT_Phase_1_AMR_Annotation_R4.tgz`  |
| LDC2013E117 (Proxy only) | October 14, 2013 | 8,219 | `scripts/train_LDC2013E117.sh` | `LDC2013E117.tgz` |
| [AMR Bank v1.4](http://amr.isi.edu/download.html) | November 14, 2014 | 1,562 | `scripts/train_Little_Prince.sh` | (automatically downloaded)   |

For LDC2013E117 or LDC2014E41, you will need a license for LDC DEFT project data. The trained model will go into a subdirectory of `models/` and the evaulation results will be printed and saved to
`models/directory/RESULTS.txt`.  The performance of the parser on the various datasets is in [docs/Parser
Performance](docs/Parser_Performance.md).

To train the parser on another dataset, create a [config file](docs/Config_File.md) in `scripts/` and
then do:

    . scripts/my_config_file.sh
    scripts/TRAIN.sh

The trained model will be saved into the `$MODEL_DIR` specified in the config script, and the results saved in
`$MODEL_DIR/RESULTS.txt` To run the parser with your trained model, source `my_config_file.sh` before running
`PARSE.sh`.

## Evaluating

To evaluate a trained model against a gold standard AMR file, do:

    . scripts/my_config_file.sh
    scripts/EVAL.sh gold_amr_file

The predicted output will be in `models/my_directory/gold_amr_file.parsed-gold-concepts` for the parser with oracle
concept ID, `models/my_directory/gold_amr_file.parsed` for the full pipeline, and the results saved in
`models/my_directory/gold_amr_file.results`.
