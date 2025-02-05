# TokTokClone Project Agent

## Initial Actions Required
1. Read and acknowledge the current time
2. Read `project-checklist.md` to understand current project status and update it as you complete tasks and as new features are added.
3. Calculate time remaining until next deadline based on current time
4. **VERIFY WORKING DIRECTORY**: All work MUST be done in the `reel-ai` directory. Any file paths should be prefixed with `reel-ai/`. For example:
   - ✅ `reel-ai/app/src/main/java/com/example/reel_ai/MainActivity.kt`
   - ❌ `app/src/main/java/com/example/reel_ai/MainActivity.kt`
5. **PRINT FULL PATH**: When making changes, print the full path of the file you are changing. For example:
   - ✅ `/Users/calemcnulty/Workspace/reel-ai/app/src/main/java/com/example/reel_ai/MainActivity.kt`
   - ❌ `MainActivity.kt`
6. **SANITY CHECK**: After writing the full path, check that it starts with `/Users/calemcnulty/Workspace/reel-ai/`. If it doesn't, HALT AND CATCH FIRE.
7. **ADD LOGGING**: Whenever you implement a new feature, log out its state and execution flow. In particular, be sure to log the error in every error handling block.

## Project Context
- This is an Android-based TikTok clone focusing on the creator vertical
- Development follows MVVM architecture with Clean Architecture principles
- All code should be modular, testable, and well-documented

## Key Deadlines
- MVP Deadline: Wednesday, Feb 5, 2025, 6:00 PM CST
- Week 1 Deadline: Friday, Feb 7, 2025, 6:00 PM CST
- Final Submission: Sunday, Feb 9, 2025, 6:00 PM CST

## Response Protocol
1. For each new conversation:
   - State the current time
   - Calculate and state time remaining until next deadline
   - Review project-checklist.md for current status
   - Frame all responses in context of remaining time and project status

2. When making changes:
   - Update project-checklist.md as tasks are completed
   - Ensure all code changes follow project architecture
   - Document significant decisions or changes

3. Priority Guidelines:
   - Phase 1 (PoC) features are highest priority
   - MVP features should only be started after PoC is stable
   - Polish items should not block core functionality

## Technical Requirements
- Use Kotlin with Jetpack Compose
- Follow MVVM architecture
- Implement Clean Architecture principles
- Use Firebase for backend services
- Focus on modular, maintainable code
- Keep functions under 60 lines
- Keep files under 250 lines

## Documentation Requirements
- All code changes must be documented
- Update project-checklist.md after each significant change
- Track progress percentages for each phase
  - Update progress after each completed task
  - Recalculate phase percentages based on completed vs total tasks
  - Consider task dependencies and complexity in progress calculations
- Document any blockers or risks immediately

## Communication Style
- Be direct and precise
- Focus on technical accuracy
- Provide clear rationale for decisions
- Flag any potential risks or blockers
- Always frame responses in context of project timeline 