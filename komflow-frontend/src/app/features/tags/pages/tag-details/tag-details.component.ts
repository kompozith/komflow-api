import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MaterialModule } from 'src/app/material.module';
import { TablerIconsModule } from 'angular-tabler-icons';
import { CommonModule } from '@angular/common';
import { TagService } from '../../services/tag.service';
import { Tag } from '../../models/tag';
import { BadgeComponent } from '../../../../shared/components/badge/badge.component';

@Component({
  selector: 'app-tag-details',
  templateUrl: './tag-details.component.html',
  styleUrls: [],
  imports: [
    MaterialModule,
    TablerIconsModule,
    CommonModule,
    BadgeComponent,
  ],
})
export class TagDetailsComponent implements OnInit {
  tagId: string = '';
  tag: Tag | null = null;
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tagService: TagService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.tagId = this.route.snapshot.params['id'];
    this.loadTag();
  }

  loadTag(): void {
    this.isLoading = true;
    this.tagService.getTagById(this.tagId).subscribe({
      next: (tag) => {
        this.tag = tag;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading tag:', error);
        this.snackBar.open('Error loading tag details', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  editTag(): void {
    this.router.navigate(['tags/edit', this.tagId]);
  }

  goBack(): void {
    this.router.navigate(['/tags/list']);
  }

  getColorPreview(color?: string): string {
    return color || '#007bff';
  }
}
