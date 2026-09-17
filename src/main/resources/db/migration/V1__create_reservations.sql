
CREATE EXTENSION IF NOT EXISTS btree_gist;








CREATE TABLE IF NOT EXISTS public.reservations (
                                                   id UUID PRIMARY KEY,
                                                   resource_id UUID NOT NULL,
                                                   member_id UUID NOT NULL,
                                                   start_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                                   end_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                                   status VARCHAR(20) NOT NULL
    );




DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_reservations_period'
          AND conrelid = 'public.reservations'::regclass
    ) THEN
ALTER TABLE public.reservations
    ADD CONSTRAINT ck_reservations_period
        CHECK (start_at < end_at);
END IF;
END;
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_reservations_status'
          AND conrelid = 'public.reservations'::regclass
    ) THEN
ALTER TABLE public.reservations
    ADD CONSTRAINT ck_reservations_status
        CHECK (status IN ('CONFIRMED', 'CANCELLED'));
END IF;
END;
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ex_reservations_confirmed_no_overlap'
          AND conrelid = 'public.reservations'::regclass
    ) THEN
ALTER TABLE public.reservations
    ADD CONSTRAINT ex_reservations_confirmed_no_overlap
    EXCLUDE USING gist (
                resource_id WITH =,
                tsrange(start_at, end_at, '[)') WITH &&
            )
            WHERE (status = 'CONFIRMED');
END IF;
END;
$$;