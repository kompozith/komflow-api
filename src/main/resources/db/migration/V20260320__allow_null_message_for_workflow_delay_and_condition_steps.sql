-- DELAY and CONDITION workflow steps do not require a linked message.
ALTER TABLE IF EXISTS komflow.msg_event_registration_workflow_steps
    ALTER COLUMN msg_message_id DROP NOT NULL;
