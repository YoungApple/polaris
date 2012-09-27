package com.codingstory.polaris.parser;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class FieldDeclaration extends TokenBase implements VariableDeclaration {

    private final FullMemberName name;
    private final TypeReference typeReference;

    public static class Builder {
        private Span span;
        private FullMemberName name;
        private TypeReference typeReference;

        public Builder setSpan(Span span) {
            this.span = span;
            return this;
        }

        public Builder setName(FullMemberName name) {
            this.name = name;
            return this;
        }

        public Builder setTypeReference(TypeReference typeReference) {
            this.typeReference = typeReference;
            return this;
        }

        public FieldDeclaration build() {
            Preconditions.checkNotNull(span);
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(typeReference);
            return new FieldDeclaration(this);
        }
    }

    public FieldDeclaration(Builder builder) {
        super(Kind.FIELD_DECLARATION, builder.span);
        this.name = builder.name;
        this.typeReference = builder.typeReference;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public FullMemberName getName() {
        return name;
    }

    @Override
    public String getVariableName() {
        return name.getMemberName();
    }

    @Override
    public TypeReference getTypeReference() {
        return typeReference;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(FieldDeclaration.class)
                .add("name", name)
                .add("typeReference", typeReference)
                .toString();
    }
}
