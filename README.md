# iRap
Interest-based RDF update propagation framework

iRap is an RDF update propagation framework that propagates only interesting parts of an update from the source dataset to the target dataset. iRap filters interesting parts of changesets from the source dataset based on graph-pattern-based interest expressions registered by a target dataset user.
The Interest-based RDF update propagation (iRap) framework was implemented using Jena-ARQ. It is provided as open-source and consists of three modules: Interest Manager (IM), Changeset Manager (CM) and Interest Evaluator (IE), each of which each can be extended to accommodate new or improved functionality.
This repository provides source code and evaluation material for iRap framework.